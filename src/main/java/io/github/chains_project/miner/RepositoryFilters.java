package io.github.chains_project.miner;

import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHEvent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTreeEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * The RepositoryFilters class contains predicates over GitHub repositories
 * and methods that can be used to filter for repositories having certain properties.
 */
public class RepositoryFilters {

    /**
     * Check whether the given repository contains any workflows that is run on PRs.
     */
    public static final Predicate<GHRepository> hasPullRequestWorkflows = repository -> {
        var workflowIterator = repository.queryWorkflowRuns()
                .event(GHEvent.PULL_REQUEST)
                .list().withPageSize(1)
                .iterator();
        return workflowIterator.hasNext();
    };
    private static final Logger log = LoggerFactory.getLogger(RepositoryFilters.class);

    private RepositoryFilters() { /* Nothing to see here... */ }

    /**
     * Identifies the type of project (Maven, Gradle, npm, yarn, pip, RubyGems, Helm, Conda, Composer, NuGet, Bower, Cargo)
     * and checks for the existence of lockfiles in the main branch of the GitHub repository.
     *
     * @return a ProjectInfo object containing the identified ProjectType and a boolean indicating whether a lockfile exists.
     */
    public static ProjectInfo identifyProjectTypeAndLockfile(GHRepository repository) {
        try {
            List<GHTreeEntry> treeEntries = repository.getTree(repository.getDefaultBranch()).getTree();
            List<ProjectType> lockfiles = new ArrayList<>();

            boolean hasGradle = false, hasNpm = false, hasYarn = false, hasPipEnv = false, hasRubyGems = false,
                    hasHelm = false, hasComposer = false, hasNuGet = false, hasBower = false, hasCargo = false;
            boolean hasGradleLock = false, hasNpmLock = false, hasYarnLock = false, hasPipEnvLock = false,
                    hasRubyGemsLock = false, hasHelmLock = false, hasComposerLock = false, hasNuGetLock = false,
                    hasBowerLock = false, hasCargoLock = false, hasShrinkwrap = false, hasPnpmLock = false,
                    hasBunLock = false;

            for (GHTreeEntry entry : treeEntries) {
                String path = entry.getPath();
                // if (path.contains("Cargo.toml")) {
                //     hasCargo = true;
                // } else if (path.contains("Cargo.lock")) {
                //     hasCargoLock = true;
                // }
//                if (path.contains("build.gradle")) {
//                    hasGradle = true;
//                } else if (path.contains("gradle.lockfile")) {
//                    hasGradleLock = true;
               if (path.contains("package.json")) {
                   // the content of the package.json will be checked again later.
                   hasNpm = true;
               } else if (path.contains("package-lock.json")) {
                   hasNpmLock = true;
                   System.out.println("npm lock");
               } else if (path.contains("yarn.lock")) {
                   hasYarnLock = true;
               } else if (path.contains("npm-shrinkwrap.json")) {
                   hasShrinkwrap = true;
               } else if (path.contains("pnpm-lock.yaml")) {
                   hasPnpmLock = true;
               } else if (path.contains("bun.lockb")) {
                   hasBunLock = true;
               }
            //    else if (path.contains("Pipfile")) {
//                    hasPipEnv = true;
//                    System.out.println("pip");
//                } else if (path.contains("Pipfile.lock")) {
//                    hasPipEnvLock = true;
//                } else if (path.contains("Gemfile")) {
//                    hasRubyGems = true;
//                    System.out.println("ruby gem");
//                } else if (path.contains("Gemfile.lock")) {
//                    hasRubyGemsLock = true;
//                } else if (path.contains("Chart.yaml")) {
//                    hasHelm = true;
//                    System.out.println("helm");
//                } else if (path.contains("Chart.lock")) {
//                    hasHelmLock = true;
//                } else if (path.contains("composer.json")) {
//                    hasComposer = true;
//                    System.out.println("composer");
//                } else if (path.contains("composer.lock")) {
//                    hasComposerLock = true;
//                } else if (path.contains("bower.json")) {
//                    hasBower = true;
//                    System.out.println("bower");
//                } else if (path.contains("bower.lock")) {
//                    hasBowerLock = true;
//                } else if (path.contains("Cargo.toml")) {
//                    hasCargo = true;
//                    System.out.println("cargo");
//                } else if (path.contains("Cargo.lock")) {
//                    hasCargoLock = true;
//                } else if (path.contains("packages.config")) {
//                    hasNuGet = true;
//                    System.out.println(".net");
//                } else if (path.contains("packages.lock.json")) {
//                    hasNuGetLock = true;
//                }
            }
//            if (hasGradle) {
//                System.out.println("gradle");
//                return new ProjectInfo(repository, ProjectType.GRADLE, hasGradleLock);
//            }
            if (hasNpm) {
                if (hasShrinkwrap) {
                    System.out.println("shrinkwrap");
                    lockfiles.add(ProjectType.NPMSHRINK);
                }
                if (hasYarnLock) {
                    System.out.println("yarn");
                    lockfiles.add(ProjectType.YARN);
                }
                if (hasPnpmLock) {
                    System.out.println("pnpm");
                    lockfiles.add(ProjectType.PNPM);
                }
                if (hasNpmLock) {
                    System.out.println("npm");
                    lockfiles.add(ProjectType.NPM);
                }
                if (hasBunLock) {
                    System.out.println("bun");
                    lockfiles.add(ProjectType.BUN);
                }
                if (lockfiles.isEmpty()) {
                    System.out.println("empty");
                    return new ProjectInfo(repository, Collections.singletonList(ProjectType.npm), false);
                }
                return new ProjectInfo(repository, lockfiles, true);
            }


//            if (hasPipEnv) return new ProjectInfo(repository, ProjectType.PIP, hasPipEnvLock);
//            if (hasRubyGems) return new ProjectInfo(repository, ProjectType.RUBYGEMS, hasRubyGemsLock);
//            if (hasHelm) return new ProjectInfo(repository, ProjectType.HELM, hasHelmLock);
//            if (hasComposer) return new ProjectInfo(repository, ProjectType.COMPOSER, hasComposerLock);
//            if (hasNuGet) return new ProjectInfo(repository, ProjectType.NUGET, hasNuGetLock);
//            if (hasBower) return new ProjectInfo(repository, ProjectType.BOWER, hasBowerLock);
//            if (hasCargo) return new ProjectInfo(repository, ProjectType.CARGO, hasCargoLock);

        } catch (IOException e) {
            throw new RuntimeException("Failed to check repository structure", e);
        }
        System.out.println("unknown");
        return null;
    }

    /**
     * Check if a given repository has sufficient number of commits.
     */
    public static boolean hasSufficientNumberOfCommits(GHRepository repository, int minNumberOfCommits) {
        try {
            return repository.listCommits().toList().size() >= minNumberOfCommits;
        } catch (IOException e) {
            log.error("Search for GitHub repo {} failed : ", repository.getFullName(), e);
            return false;
        }
    }

    /**
     * Check if a given repository has sufficient number of contributors.
     */
    public static boolean hasSufficientNumberOfContributors(GHRepository repository, int minNumberOfContributors) {
        try {
            return repository.listContributors().toList().size() >= minNumberOfContributors;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isLastCommitWithinThreeMonths(GHRepository repository) {
        try {
            List<GHCommit> commits = repository.listCommits().toList();
            if (!commits.isEmpty()) {
                LocalDate lastCommitDate = commits.get(0).getCommitDate().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDate();
                LocalDate threeMonthsAgo = LocalDate.now().minus(3, ChronoUnit.MONTHS);
                return lastCommitDate.isAfter(threeMonthsAgo);
            }
        } catch (IOException e) {
            log.error("Error retrieving commits for repository: " + repository.getFullName(), e);
        }
        return false;
    }

    /**
     * Enum representing different project types based on the build system.
     */
    public enum ProjectType {
        GRADLE, NPM, YARN, NPMSHRINK, PNPM, npm, PIP, RUBYGEMS, HELM, COMPOSER, NUGET, BOWER, CARGO, BUN
    }
}
