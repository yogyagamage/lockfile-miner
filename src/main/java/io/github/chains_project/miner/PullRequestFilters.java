package io.github.chains_project.miner;

import org.kohsuke.github.GHPullRequest;

import java.io.IOException;
import java.util.Date;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * The PullRequestFilters class contains predicates over GitHub repositories
 * that can be used to filter for pull requests having certain properties.
 */
public class PullRequestFilters {

    private static final Pattern NPM_LOCKFILE_CHANGE = Pattern.compile("^[+]{3}.*package-lock.json$", Pattern.MULTILINE);
    /**
     * Checks whether a pull request was created before the given date.
     *
     * @param cutoffDate The point in time which the PR must have been created before.
     * @return a {@link Predicate} over {@link org.kohsuke.github.GHPullRequest}s returning
     * true if a PR was created before the given date, false otherwise.
     */
    public static final Predicate<GHPullRequest> changesOnlyLockfile = pr -> {
        String patch = GitPatchCache.get(pr).orElse("");
        if (NPM_LOCKFILE_CHANGE.matcher(patch).find()) {
            return true;
        } else {
            // If we don't match the predicate, the pull request will get filtered out,
            // and we can remove it from the cache.
            GitPatchCache.remove(pr);
            return false;
        }
    };
    private static final Pattern GRADLE_LOCKFILE_CHANGE = Pattern.compile("^[+]{3}.*gradle.lockfile$", Pattern.MULTILINE);

    private PullRequestFilters() { /* Nothing to see here... */ }

    /**
     * Checks whether a pull request was created before the given date.
     *
     * @param cutoffDate The point in time which the PR must have been created before.
     * @return a {@link Predicate} over {@link org.kohsuke.github.GHPullRequest}s returning
     * true if a PR was created before the given date, false otherwise.
     */
    public static Predicate<GHPullRequest> createdBefore(Date cutoffDate) {
        return pr -> {
            try {
                return pr.getCreatedAt().before(cutoffDate);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
