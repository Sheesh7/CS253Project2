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
        BetterHashTable<String, User> userTable = new BetterHashTable<>();
        BetterHashTable<String, Machine> machineTable = new BetterHashTable<>();
        MinHeap<Machine> machineHeap = new MinHeap<>();
        ArrayList<FinishedJob<?,?,?>> jobList = new ArrayList<>();
    
        try {
            BufferedReader bf = new BufferedReader(new FileReader(filename));
            String line = bf.readLine();
            if (line == null) {
                return list;
            }
            String[] first = line.split("\\s+");
            int U = Integer.parseInt(first[0]);
            int M = Integer.parseInt(first[1]);
            int N = Integer.parseInt(first[2]);
            
            for (int i = 0; i < U; i++) {
                line = bf.readLine();
                String[] parts = line.split("\t");
                String userID = parts[0];
                int weight = Integer.parseInt(parts[parts.length - 1]);
                String email = parts[parts.length - 2];
                StringBuilder nameBuilder = new StringBuilder();
                for (int j = 1; j < parts.length - 2; j++) {
                    nameBuilder.append(parts[j]);
                    if (j < parts.length - 3) {
                        nameBuilder.append(" ");
                    }
                }
                String name = nameBuilder.toString();
                User user = new User(userID, name, email, weight);
                userTable.insert(userID, user);
            }

            for (int i = 0; i < M; i++){
                line = bf.readLine();
                String[] parts = line.split("\t");
                int speed = Integer.parseInt(parts[parts.length - 1]);
                StringBuilder nameBuilder = new StringBuilder();
                for (int j = 0; j < parts.length - 1; j++) {
                    nameBuilder.append(parts[j]);
                    if (j < parts.length - 2) {
                        nameBuilder.append(" ");
                    }
                }
                String name = nameBuilder.toString();
                Machine machine = new Machine(name, speed);
                machineTable.insert(name, machine);
                machineHeap.add(machine);
            }
            
            

            for (int i = 0; i < N; i++) {
                line = bf.readLine();
                String[] parts = line.split("\t");
                int jobID = Integer.parseInt(parts[0]);
                StringBuilder nameBuilder = new StringBuilder();
                for (int j = 1; j < parts.length - 3; j++) {
                    if (j > 1) {
                        nameBuilder.append(" ");
                    }
                    nameBuilder.append(parts[j]);
                }
                String name = nameBuilder.toString();
                String userID = parts[parts.length - 3];
                int subTime = Integer.parseInt(parts[parts.length - 2]);
                int runTime = convertToSecs(parts[parts.length - 1]);
                Job job = new Job(jobID, name, userID,subTime,runTime);
                User user  = userTable.get(userID);
                FinishedJob<?,?,?> unfinished = new FinishedJob<>(user, null, job);
                jobList.add(unfinished);  
            }
            

        } catch (IOException e) {
            //This should never happen... uh oh o.o
            System.err.println("ATTENTION TAs: Couldn't find test file: \"" + filename + "\":: " + e.getMessage());
            System.exit(1);
        }

        for (int i =1; i < jobList.size(); i++) {
            FinishedJob<?,?,?> key = jobList.get(i);
            int j = i - 1;
            while (j >= 0 && (jobList.get(j).job.getSubmissionTime() > key.job.getSubmissionTime() || (jobList.get(j).job.getSubmissionTime() == key.job.getSubmissionTime() && jobList.get(j).compareTo(key) > 0))) {
                jobList.set(j + 1, jobList.get(j));
                j--;
                jobList.set(j + 1, key);
            }
        }
        for (FinishedJob<?,?,?> unfinished : jobList) {
            Job job = unfinished.job;
            User user = unfinished.user;
            Machine machine = machineHeap.removeMin();
            int startTime = Math.max(job.getSubmissionTime(), machine.getFinishingTime());
            int runTime = (int) Math.ceil((double) job.getRunningTime() / machine.getSpeed());
            int compTime = startTime + runTime;
            machine.setFinishingTime(compTime);
            machineHeap.add(machine);
            FinishedJob<?,?,?> finished = new FinishedJob<>(user,machine, job, startTime, compTime);
            list.add(finished);
        }
        for (int  i = 1; i <list.size(); i++) {
            FinishedJob<?,?,?> key = list.get(i);
            int j = i - 1;
            while (j >= 0 && list.get(j).completionTime > key.completionTime) {
                list.set(j + 1, list.get(j));
                j--;
            }
            list.set(j + 1, key);
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
