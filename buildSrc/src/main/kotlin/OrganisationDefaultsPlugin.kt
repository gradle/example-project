import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler

/**
 * A module set lists all modules that, by default, belong together (typically lists all modules of a group)
 */
private data class ModuleSet(val group: String, val modules: List<String>, val reason: String)

/**
 * If alignment needs to be tweaked, e.g. if one of the modules contains a bug, alignment customizations can be used.
 */
private data class AlignmentCustomization(val excludes: List<String> = emptyList())

open class OrganisationDefaultsPlugin: Plugin<Project> {

    private val alignfamilyModuleSet = ModuleSet(
            group = "alignmentrule",
            modules = listOf("align0", "align1"),
            reason = "All alignfamily modules provide the 'alignfamily' functionality and are versioned together."
    )

    private val directalignModuleSet = ModuleSet(
            group = "alignmentrule",
            modules = listOf("direct-align0", "direct-align1"),
            reason = "All direct-align modules provide the 'direct-align' functionality and are versioned together."
    )

    private val excludealignModuleSet = ModuleSet(
            group = "alignmentrule",
            modules = listOf("exclude-align0", "exclude-align1", "exclude-align2"),
            reason = "All exclude-align modules provide the 'exclude-align' functionality and are versioned together."
    )

    private val alignforceModuleSet = ModuleSet(
            group = "alignmentrule",
            modules = listOf("alignforce0", "alignforce1"),
            reason = "All alignforce modules provide the 'alignforce' functionality and are versioned together."
    )

    override fun apply(project: Project) {
        project.dependencies.align(alignfamilyModuleSet)
        project.dependencies.align(directalignModuleSet)
        project.dependencies.align(excludealignModuleSet, AlignmentCustomization(excludes = listOf("exclude-align2")))
        project.dependencies.align(alignforceModuleSet)
    }
}

/**
 * The alignment pattern:
 * Each known version of a module in a modules set defines a constraint to
 * all other modules of the set with the same version.
 */
private fun DependencyHandler.align(moduleSet: ModuleSet, customization: AlignmentCustomization = AlignmentCustomization()) {
    moduleSet.modules.filter { !customization.excludes.contains(it) }.forEach { moduleName ->
        this.components.withModule("${moduleSet.group}:$moduleName") {
            val version = id.version
            allVariants {
                withDependencyConstraints {
                    moduleSet.modules.filter { moduleName != it && !customization.excludes.contains(it) }.forEach {
                        add("${moduleSet.group}:$it:$version") {
                            because(moduleSet.reason)
                        }
                    }
                }
            }
        }
    }
}