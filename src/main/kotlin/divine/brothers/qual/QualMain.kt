package divine.brothers.qual

import divine.brothers.readFromClasspath
import java.io.File


fun main() {

    val files = listOf(
        "a_an_example.in.txt",
        "b_better_start_small.in.txt",
        "c_collaboration.in.txt",
        "d_dense_schedule.in.txt",
        "e_exceptional_skills.in.txt",
        "f_find_great_mentors.in.txt",
    ).map { "qual_data/$it" }

    for (file in files) {
        println("Attempting solution for $file...")

        val input = readFromClasspath(file).useLines(::parse)

        println("Contributors: ${input.contributors.size}, Projects: ${input.projects.size}, Skills: ${input.skills.size}")

        val solution = FirstApproach(input)

        val assignments = solution.findAssignments()

        val outFile = File( "qual" , file.substringAfterLast("/").replace(".in.txt", ".out.txt"))
        outFile.parentFile.mkdir()
        outFile.bufferedWriter().use { writer ->
            writer.appendLine(assignments.size.toString())
            for ((projectId, contribIds) in assignments) {
                writer.appendLine(solution[projectId].name)
                writer.appendLine(contribIds.joinToString(" ") { solution[it].name })
            }
        }

        println(assignments.size)

        println("---------------------------------")

    }



}
