package Items;

public class Job {
    protected int id;
    protected String name;
    protected String userID;
    protected int submissionTime;
    protected int runningTime;

    // Constructor
    public Job(int id, String name, String userID, int submissionTime, int runningTime) {
        this.id = id;
        this.name = name;
        this.userID = userID;
        this.submissionTime = submissionTime;
        this.runningTime = runningTime;
    }

    // getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getUserID() { return userID; }
    public int getSubmissionTime() { return submissionTime; }
    public int getRunningTime() { return runningTime; }

    // setters
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setUserID(String userID) { this.userID = userID; }
    public void setSubmissionTime(int submissionTime) { this.submissionTime = submissionTime; }
    public void setRunningTime(int runningTime) { this.runningTime = runningTime; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Job job = (Job) o;
        return (id == job.id) && name.equals(job.name) && userID.equals(job.userID)
                && (submissionTime == job.submissionTime) && (runningTime == job.runningTime);
    }

    @Override
    public String toString() {
        return "Job{" + "id=" + id + ", name='" + name + '\'' + ", userID='" + userID +
                ", submissionsTime=" + submissionTime + runningTime + '}';
    }
}