package Items;

public class Machine implements Comparable<Machine> {
    protected String name;
    protected int speed;
    protected int finishingTime;

    // Constructor
    public Machine(String name, int speed) {
        this.name = name;
        this.speed = speed;
        this.finishingTime = 0;
    }

    // getters
    public String getName() { return name; }
    public int getSpeed() { return speed; }
    public int getFinishingTime() { return finishingTime; }

    // setters
    public void setName(String name) { this.name = name; }
    public void setSpeed(int speed) { this.speed = speed; }
    public void setFinishingTime(int finishingTime) { this.finishingTime = finishingTime; }

    @Override
    public int compareTo(Machine o) {
        return finishingTime == o.finishingTime ? name.compareTo(o.name) : finishingTime - o.finishingTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Machine machine = (Machine) o;
        return name.equals(machine.name) && speed == machine.speed && finishingTime == machine.finishingTime;
    }

    @Override
    public String toString() {
        return "Machine{" + "name=" + name + ", speed=" + speed + ", finishingTime=" + finishingTime + '}';
    }
}