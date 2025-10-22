package JobScheduler;

import CommonUtils.BetterHashTable;
import CommonUtils.MinHeap;
import Items.Job;
import Items.Machine;
import Items.User;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Manages everything regarding the Job Scheduling on the new CS Computing Cluster.
 */
public class CSJobScheduler implements CSJobSchedulerInterface {


    public ArrayList<FinishedJob<?, ?, ?>> getCompletionTimes(String filename) {
        ArrayList<FinishedJob<?, ?, ?>> list = new ArrayList<>();

        try {
            BufferedReader bf = new BufferedReader(new FileReader(filename));
            String[] firstLine = bf.readLine().trim().split("\\s+");
            int U = Integer.parseInt(firstLine[0]);
            int M = Integer.parseInt(firstLine[1]);
            int N = Integer.parseInt(firstLine[2]);

            BetterHashTable<String, User> users = new BetterHashTable<>();
            for (int i = 0; i < U; i++) {
                String[] parts = bf.readLine().trim().split("\t");
                String userID = parts[0];
                int weight = Integer.parseInt(parts[parts.length - 1]);
                String email = parts[parts.length - 2];
                StringBuilder nameBuilder = new StringBuilder();
                for (int j = 1; j < parts.length - 2; j++) {
                    if (j > 1) nameBuilder.append(" ");
                    nameBuilder.append(parts[j]);
                }
                String name = nameBuilder.toString();
                users.insert(userID, new User(userID, name, email, weight));
            }

            MinHeap<Machine> machineHeap = new MinHeap<>();

            for (int i = 0; i < M; i++) {
                String[] parts = bf.readLine().trim().split("\t");
                String name = parts[0];
                int speed = Integer.parseInt(parts[1]);
                Machine m = new Machine(name, speed);
                machineHeap.add(m);
            }

            ArrayList<Job> jobList = new ArrayList<>();
            for (int i = 0; i < N; i++) {
                String[] parts = bf.readLine().trim().split("\t");
                int id = Integer.parseInt(parts[0]);
                int runTime = convertToSecs(parts[parts.length - 1]);
                int subTime = Integer.parseInt(parts[parts.length - 2]);
                String userID = parts[parts.length - 3];
                StringBuilder jobNameBuilder = new StringBuilder();
                for (int j = 1; j < parts.length - 3; j++){
                    if (j > 1) jobNameBuilder.append(" ");
                    jobNameBuilder.append(parts[j]);
                }
                String jobName = jobNameBuilder.toString();
                jobList.add(new Job(id, jobName, userID, subTime, runTime));
            }

            MinHeap<FinishedJob<?, ?, ?>> waitingJobs = new MinHeap<>();
            int currentIndex = 0;
            int currentTime = 0;

            while (currentIndex < jobList.size() || waitingJobs.size() > 0) {
                while (currentIndex < jobList.size() && jobList.get(currentIndex).getSubmissionTime() <= currentTime) {
                    Job job = jobList.get(currentIndex);
                    User user = users.get(job.getUserID());
                    FinishedJob<?, ?, ?> fJob = new FinishedJob<>(user, null, job);
                    waitingJobs.add(fJob);
                    currentIndex++;
                }
                while (waitingJobs.size() > 0 && machineHeap.size() > 0 && machineHeap.peekMin().getFinishingTime() <= currentTime) {
                    Machine machine = machineHeap.removeMin();
                    FinishedJob<?, ?, ?> fJob = waitingJobs.removeMin();
                    Job job = fJob.job;
                    int actualRunningTime = (int) Math.ceil((double) job.getRunningTime() / machine.getSpeed());
                    int scheduledTime = Math.max(currentTime, Math.max(job.getSubmissionTime(), machine.getFinishingTime()));
                    int completionTime = scheduledTime + actualRunningTime;
                    machine.setFinishingTime(completionTime);
                    ((FinishedJob<User, Machine, Job>) fJob).machine = machine;
                    fJob.scheduledTime = scheduledTime;
                    fJob.completionTime = completionTime;
                    list.add(fJob);
                    machineHeap.add(machine);
                }
                int nextJobTime = currentIndex < jobList.size() ? jobList.get(currentIndex).getSubmissionTime() : Integer.MAX_VALUE;
                int nextMachineTime = Integer.MAX_VALUE;
                if (waitingJobs.size() > 0) {
                    nextMachineTime = machineHeap.peekMin().getFinishingTime();
                }
                int nextTime = Math.min(nextJobTime,nextMachineTime);
                if (nextTime == Integer.MAX_VALUE) {
                    break;
                }
                currentTime = nextTime;
            }
            bf.close();
            list.sort((a,b) -> {if (a.completionTime != b.completionTime) {
                return a.completionTime - b.completionTime;
            } else {
                return ((Job)a.job).getId() - ((Job)b.job).getId();
            }
            });
        } catch (IOException e) {
            //This should never happen... uh oh o.o
            System.err.println("ATTENTION TAs: Couldn't find test file: \"" + filename + "\":: " + e.getMessage());
            System.exit(1);
        }
        return list;
    }
    private int convertToSecs(String time) {
        String[] parts = time.split(":");
        if (parts.length == 1) {
            return Integer.parseInt(parts[0]);
        } else if (parts.length == 2) {
            int m = Integer.parseInt(parts[0]);
            int s = Integer.parseInt(parts[1]);
            return m * 60 + s;
        } else if (parts.length == 3) {
            int h = Integer.parseInt(parts[0]);
            int m = Integer.parseInt(parts[1]);
            int s = Integer.parseInt(parts[2]);
            return h * 3600 + m * 60 + s;
        } else {
            throw new IllegalArgumentException("Invalid Time: " + time);
        }
    }
}