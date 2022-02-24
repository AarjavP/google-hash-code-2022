package divine.brothers.qual

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap


@JvmInline
value class Skill(val id: Int) {
    override fun toString(): String = id.toString()
}

@JvmInline
value class ContributorId(val id: Int)

@JvmInline
value class ProjectId(val id: Int)

data class SkillLevel(
    val skill: Skill,
    var level: Int,
)

data class Contributor(
    val id: ContributorId,
    val name: String,
    val skills: MutableList<SkillLevel>,
) {
    operator fun get(skill: Skill): Int = skills.firstOrNull { it.skill == skill }?.level ?: 0
    infix fun canFill(requirement: SkillLevel): Boolean = get(requirement.skill) >= requirement.level
    infix fun learn(skill: Skill) {
        val skillLevel = skills.firstOrNull { it.skill == skill } ?: SkillLevel(skill, 0).also { skills += it }
        skillLevel.level++
    }
}

data class Project(
    val id: ProjectId,
    val name: String,
    val duration: Int,
    val bestBefore: Int,
    val score: Int,
    val roles: List<SkillLevel>,
)

data class Input(
    val contributors: List<Contributor>,
    val projects: List<Project>,
    //id to name
    val skills: BiMap<Skill, String>,
)

fun parse(lines: Sequence<String>): Input {
    val iter = lines.iterator()
    val (numContributors, numProjects) = iter.next().split(" ").map { it.toInt() }

    var skillCounter = 0
    val skillAliases: BiMap<Skill, String> = HashBiMap.create()
    val skillNameToId = skillAliases.inverse()

    fun readSkills(count: Int): ArrayList<SkillLevel> {
        val ret = ArrayList<SkillLevel>(count)
        repeat(count) {
            val skillHeader = iter.next()
            val skillName = skillHeader.substringBefore(" ")
            val skillLevel = skillHeader.substringAfter(" ").toInt()
            val skill = skillNameToId.computeIfAbsent(skillName) { Skill(skillCounter++) }
            ret += SkillLevel(skill, skillLevel)
        }
        return ret
    }

    val contributors = ArrayList<Contributor>(numContributors)
    for (contributorId in 0 until numContributors) {
        val contributorHeader = iter.next()
        val contributorName = contributorHeader.substringBefore(" ")
        val contributorSkillCount = contributorHeader.substringAfter(" ").toInt()
        val skills = readSkills(contributorSkillCount)
        contributors += Contributor(ContributorId(contributorId), contributorName, skills)
    }


    val projects = ArrayList<Project>(numProjects)
    for (projectId in 0 until numProjects) {
        val projectHeader = iter.next().split(" ")
        val projectName = projectHeader[0]
        val projectDuration = projectHeader[1].toInt()
        val projectScore = projectHeader[2].toInt()
        val projectBestBy = projectHeader[3].toInt()
        val projectNumRoles = projectHeader[4].toInt()

        val roles = readSkills(projectNumRoles)
        projects += Project(
            id = ProjectId(projectId),
            name = projectName,
            duration = projectDuration,
            bestBefore = projectBestBy,
            score = projectScore,
            roles = roles
        )
    }


    return Input(
        contributors = contributors,
        projects = projects,
        skills = skillAliases,
    )
}

