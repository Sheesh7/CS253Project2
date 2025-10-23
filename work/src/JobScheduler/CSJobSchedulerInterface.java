package JobScheduler;

import Items.Job;
import Items.Machine;
import Items.User;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Interface for the CS Job Scheduler. The implementing class should follow the
 * specifications
 * listed in the project description ("Project 2"). You may NOT use any
 * Stack/Queue/priority queue/hash table
 * objects or any similar objects or implementations from the Java standard
 * libraries.
 *
 * <bold>253 students: you may use any of the data structures you have
 * previously created, but may not use
 * any Java library for stacks, queues, min heaps/priority queues, or hash
 * tables (or any similar classes).</bold>
 */
public interface CSJobSchedulerInterface {
    /**
     * Class used to store and retrieve job completion times
     * 
     * @param <E> type of job we are dealing with
     */
    class FinishedJob<U extends User, M extends Machine, J extends Job> implements Comparable<FinishedJob<?, ?, ?>> {
        int scheduledTime, completionTime;
        U user;
        M machine;
        J job;

        public FinishedJob(U u, M m, J j) {
            this.user = u;
            this.machine = m;
            this.job = j;
            this.scheduledTime = -1;
            this.completionTime = -1;
        }

        // constructor with aptly-named fields
        public FinishedJob(U u, M m, J j, int scheduledTime0, int completionTime0) {
            this.user = u;
            this.machine = m;
            this.job = j;
            scheduledTime = scheduledTime0;
            completionTime = completionTime0;
        }

        /**
         *
         * @param other a different finished job
         * @return which job should be printed first following compareTo specifications
         */
        @Override
        public int compareTo(FinishedJob other) {
            int thisPriority = this.job.getSubmissionTime() + this.job.getRunningTime() - this.user.getWeight();
            int otherPriority = other.job.getSubmissionTime() + other.job.getRunningTime() - other.user.getWeight();

            if (thisPriority != otherPriority) {
                return thisPriority - otherPriority;
            }
            if (this.user.getWeight() != other.user.getWeight()) {
                return other.user.getWeight() - this.user.getWeight();
            }
            if (this.job.getRunningTime() != other.job.getRunningTime()) {
                return this.job.getRunningTime() - other.job.getRunningTime();
            }
            if (this.job.getSubmissionTime() != other.job.getSubmissionTime()) {
                return this.job.getSubmissionTime() - other.job.getSubmissionTime();
            }
            return this.job.getId() - other.job.getId();
        }

        // default Intellij-generated equals function
        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            FinishedJob<?, ?, ?> that = (FinishedJob<?, ?, ?>) o;
            return Objects.equals(user, that.user) && Objects.equals(machine, that.machine) &&
                    Objects.equals(job, that.job) && scheduledTime == that.scheduledTime &&
                    completionTime == that.completionTime;
        }

        /**
         * To string method for printing
         * 
         * @return string version of object
         */
        @Override
        public String toString() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss")
                    .withZone(ZoneId.of("America/Indianapolis"));
            String sub = formatter.format(Instant.ofEpochSecond(job.getSubmissionTime()));
            String comp = formatter.format(Instant.ofEpochSecond(completionTime));
            int waitSecs = scheduledTime - job.getSubmissionTime();
            int hours = waitSecs / 3600;
            int min = (waitSecs % 3600) / 60;
            int sec = waitSecs % 60;
            String waitTime = String.format("%d:%02d:%02d", hours, min, sec);

            return job.getId() + "\t" + job.getName() + "\t" + user.getName() + "\t" + user.getEmail() + "\t"
                    + machine.getName() + "\t" + sub + "\t" + comp + "\t " + waitTime;
        }
    }

    /**
     * Gets the cleaning times per the specifications.
     *
     * @param filename file to read input from
     * @return the list of finished jobs, as per the specifications
     */
    ArrayList<FinishedJob<?, ?, ?>> getCompletionTimes(String filename);
}
