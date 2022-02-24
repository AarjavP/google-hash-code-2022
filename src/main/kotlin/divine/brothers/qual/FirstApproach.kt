package divine.brothers.qual

import com.google.common.collect.BiMap

class FirstApproach(input: Input) {
    val contributors: List<Contributor> = input.contributors
    val projects: List<Project> = input.projects
    val skills: BiMap<Skill, String> = input.skills

    operator fun get(id: ContributorId): Contributor = contributors[id.id]!!
    operator fun get(id: ProjectId): Project = projects[id.id]!!

    sealed class RoleFulfillmentOptions(val priority: Int) {
        data class LearningMember(val contribId: ContributorId): RoleFulfillmentOptions(2)
        data class SoloMember(val contribId: ContributorId): RoleFulfillmentOptions(1)
        data class MentorCombo(val mentoree: ContributorId, val mentors: Set<ContributorId>): RoleFulfillmentOptions(3)
    }

    fun findAssignments(): Map<ProjectId, List<ContributorId>> {

        // remaining days of project to projectIds
        val ongoingProjects = mutableMapOf<Int, MutableSet<ProjectId>>()
        var daysPassed = 0
        val assignments = mutableMapOf<ProjectId, List<ContributorId>>()
        val remainingProjects = projects.toMutableList()
        val availableContributors = contributors.mapTo(mutableSetOf()) { it.id }

        val skillToContributor = mutableMapOf<Skill, MutableMap<Int, MutableSet<ContributorId>>>()
        for (contributor in contributors) {
            for (skill in contributor.skills) {
                val levelsMap = skillToContributor.computeIfAbsent(skill.skill) { mutableMapOf() }
                val candidates = levelsMap.computeIfAbsent(skill.level) { mutableSetOf() }
                candidates += contributor.id
            }
        }

        // Returns completed projects
        fun shiftOngoingProjects(): Set<ProjectId> {
            if (ongoingProjects.isEmpty()) return emptySet()
            val daysRemaining = ongoingProjects.keys.sorted()
            val daysSkipped = daysRemaining.first()
            daysPassed += daysSkipped
            val completedProjects = ongoingProjects.remove(daysSkipped)!!
            for (days in daysRemaining.drop(1)) {
                val temp = ongoingProjects.remove(days)!!
                ongoingProjects[days - daysSkipped] = temp
            }
            return completedProjects
        }

        fun freeContributorsOf(projectId: ProjectId) {
            availableContributors.addAll(assignments[projectId]!!)
        }

        fun findMentors(requiredSkill: SkillLevel, requiredOneOf: List<SkillLevel>): Set<ContributorId> {
            val candidateMentors = skillToContributor[requiredSkill.skill]?.mapNotNullTo(mutableSetOf()) { entry ->
                entry.value.takeIf { entry.key >= requiredSkill.level }?.filter { it in availableContributors }
            }?.flatten() ?: return emptySet()
            return candidateMentors.filterTo(mutableSetOf()) { candidateId ->
                val candidate = get(candidateId)
                requiredOneOf.any { candidate canFill it }
            }
        }
        fun teach(contribId: ContributorId, skill: Skill) {
            val contributor = get(contribId)
            val beforeLevel = contributor[skill]
            if (beforeLevel == 0) {
                skillToContributor.computeIfAbsent(skill) { mutableMapOf() }
                    .computeIfAbsent(1) { mutableSetOf() }
                    .add(contribId)
            } else {
                val levelToContrib = skillToContributor[skill]!!
                levelToContrib[beforeLevel]!!.remove(contributor.id)
                levelToContrib.computeIfAbsent(beforeLevel+1) { mutableSetOf() }
                    .add(contributor.id)
            }
            contributor learn skill
        }

        fun attemptAssignmentFor(project: Project) {
            val options = project.roles.map { role ->
                val roleOptions = mutableListOf<RoleFulfillmentOptions>()
                val roleMentors = findMentors(role, project.roles.filter { it.skill != role.skill })
                for (contributorId in availableContributors) {
                    val contributor = get(contributorId)
                    val contributorLevel = contributor.get(role.skill)
                    if (contributorLevel == role.level) {
                        roleOptions += RoleFulfillmentOptions.LearningMember(contributorId)
                    } else if (contributorLevel > role.level) {
                        roleOptions += RoleFulfillmentOptions.SoloMember(contributorId)
                    } else if (contributorLevel == role.level - 1 && roleMentors.isNotEmpty()) {
                        roleOptions += RoleFulfillmentOptions.MentorCombo(contributorId, roleMentors)
                    }
                }
                roleOptions.sortByDescending { it.priority }
                roleOptions
            }
            if (options.any { it.isEmpty() }) {
                //not possible
                return
            }

            val tryingCombo = IntArray(options.size)

            var doneWithCombos = false
            fun incrementCombo() {
                var currentIndex = 0
                var carry = true
                while (carry) {
                    carry = false
                    val result = ++tryingCombo[currentIndex]
                    if (result >= options[currentIndex].size) {
                        carry = true
                        tryingCombo[currentIndex] = 0
                        currentIndex++
                        if (currentIndex >= tryingCombo.size) {
                            doneWithCombos = true
                            return
                        }
                    }
                }
            }

            fun List<RoleFulfillmentOptions>.isValid(): Boolean {
                val pickedCandidates = mapTo(mutableSetOf()) { option ->
                    when(option) {
                        is RoleFulfillmentOptions.LearningMember -> option.contribId
                        is RoleFulfillmentOptions.SoloMember -> option.contribId
                        is RoleFulfillmentOptions.MentorCombo -> option.mentoree
                    }
                }
                if (pickedCandidates.size != size) {
                    return false
                }

                for (option in filterIsInstance(RoleFulfillmentOptions.MentorCombo::class.java)) {
                    if (pickedCandidates.none { it in option.mentors }) return false
                }
                return true
            }


            while (!doneWithCombos) {
                val optionsCombo: List<RoleFulfillmentOptions> = options.mapIndexed { index, roleOptions ->
                    roleOptions[tryingCombo[index]]
                }

                if (optionsCombo.isValid()) {
                    val pickedCandidates = optionsCombo.mapTo(mutableSetOf()) { option ->
                        when(option) {
                            is RoleFulfillmentOptions.LearningMember -> option.contribId
                            is RoleFulfillmentOptions.SoloMember -> option.contribId
                            is RoleFulfillmentOptions.MentorCombo -> option.mentoree
                        }
                    }
                    for ((option, role) in optionsCombo.zip(project.roles)) {
                        val pickedCandidate = when(option) {
                            is RoleFulfillmentOptions.LearningMember -> {
                                teach(option.contribId, role.skill)
                                option.contribId
                            }
                            is RoleFulfillmentOptions.SoloMember -> option.contribId
                            is RoleFulfillmentOptions.MentorCombo -> {
                                teach(option.mentoree, role.skill)
                                option.mentoree
                            }
                        }
                        availableContributors.remove(pickedCandidate)
                    }

                    ongoingProjects.computeIfAbsent(project.duration) { mutableSetOf() }.add(project.id)
                    assignments[project.id] = pickedCandidates.toList()
                    remainingProjects.removeIf { it.id == project.id }

//                    println("Assigned $pickedCandidates to ${project.name}")
                    return
                }

                incrementCombo()
            }
        }

        while (remainingProjects.isNotEmpty()) {
            val completedProjects = shiftOngoingProjects()
            for (completedProject in completedProjects) {
                freeContributorsOf(completedProject)
            }

            val candidateProjects = remainingProjects.map {
                val potentialScore = (it.score - (daysPassed + it.duration - it.bestBefore).coerceAtLeast(0)).coerceAtLeast(0)
                val adjustedScore = potentialScore.toDouble() / it.duration
                it to adjustedScore
            }.sortedWith(
                Comparator.comparingDouble<Pair<Project, Double>> { it.second }.reversed()
                    .thenBy { it.first.bestBefore }
            )

            for (candidateProject in candidateProjects) {
                attemptAssignmentFor(candidateProject.first)
            }


            if (ongoingProjects.isEmpty()) {
                break
            }
        }
        println("remaining: " + remainingProjects.size)
        return assignments
    }


}
