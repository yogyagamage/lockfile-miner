package io.github.chains_project.miner;

import org.kohsuke.github.GHRepository;

/**
 * Class to hold project type and lockfile existence.
 */
public record ProjectInfo(GHRepository repository, RepositoryFilters.ProjectType projectType, boolean lockfileExists) {

    @Override
    public String toString() {
        return "ProjectInfo{" +
                "projectType=" + projectType +
                ", lockfileExists=" + lockfileExists +
                '}';
    }
}

