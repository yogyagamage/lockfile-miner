package io.github.chains_project.miner;

import com.fasterxml.jackson.databind.type.MapType;
import org.kohsuke.github.GHRepository;

import java.nio.file.Path;
import java.time.Instant;
import java.util.*;

/**
 * The RepositoryList class represents a collection of data regarding GitHub repositories.
 */
public class RepositoryList {

    private final Map<String, RepositoryData> repos;

    /**
     * The file that is used to persist this repository list
     */
    private final Path backingFile;

    /**
     * Create a new RepositoryList from file.
     *
     * @param jsonFile a path to a JSON file containing a RepositoryList in serialized form.
     */
    public RepositoryList(Path jsonFile) {
        backingFile = jsonFile;
        MapType jsonType = JsonUtils.getTypeFactory().constructMapType(Map.class, String.class, RepositoryData.class);
        repos = JsonUtils.readFromFile(jsonFile, jsonType);
    }

    public RepositoryData getRepoByName(String name) {
        return repos.get(name);
    }

    public Date getRepoLastCheckedDate(String repoName) {
        return repos.get(repoName).lastCheckedAt;
    }

    /**
     * Add a GitHub repository to this list.
     *
     * @param projectInfo the repository to add.
     */
    public void add(ProjectInfo projectInfo) {
        repos.put(projectInfo.repository().getFullName(), new RepositoryData(projectInfo.repository().getUrl().toString(),
                null, projectInfo.projectType(), projectInfo.lockfileExists()));
    }

    /**
     * Check if the given GitHub repository is in this list.
     *
     * @param repo the repository to look for.
     * @return true if the repository is in the list, false otherwise.
     */
    public boolean contains(GHRepository repo) {
        return repos.containsKey(repo.getFullName());
    }

    /**
     * @return the number of items in this list.
     */
    public int size() {
        return repos.size();
    }

    /**
     * @return the full names of the repositories in the list, on the form organization/project (e.g. apache/maven).
     */
    public Set<String> getRepositoryNames() {
        return repos.keySet();
    }


    /**
     * @param repoName the name of the repository on the form organization/project (e.g. apache/maven).
     * @return the last time the repository was checked for breaking updates, or the start of the UNIX epoch
     * if this repository is not yet checked.
     */
    public Date getCheckedTime(String repoName) {
        Date lastCheckedTime = repos.get(repoName).lastCheckedAt;
        return lastCheckedTime == null ? Date.from(Instant.EPOCH) : lastCheckedTime;
    }

    /**
     * @param repoName the name of the repository on the form organization/project (e.g. apache/maven).
     * @return the projectType.
     */
    public List<RepositoryFilters.ProjectType> getProjectType(String repoName) {
        return repos.get(repoName).projectType;
    }

    /**
     * @param repoName the name of the repository on the form organization/project (e.g. apache/maven).
     * @return true or false based on the lockfile existence.
     */
    public boolean isLockfilePushed(String repoName) {
        return repos.get(repoName).lockfileExists;
    }

    /**
     * Store this RepositoryList to a file in JSON format.
     */
    public void writeToFile() {
        JsonUtils.writeToFile(backingFile, repos);
    }

    public void setCheckedTime(String repo, Date from) {
    }

    record RepositoryData(String url, Date lastCheckedAt, java.util.List<RepositoryFilters.ProjectType> projectType,
                          boolean lockfileExists) {
    }
}
