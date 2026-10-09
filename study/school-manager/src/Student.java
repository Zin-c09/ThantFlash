import java.util.HashMap;
import java.util.Map;

// extends = Person ရဲ့ field/method တွေကို အမွေဆက်ခံ (継承 / inheritance)
public class Student extends Person {
    private final String className;
    // Map<Key, Value> : ဘာသာရပ်နာမည် -> အမှတ်
    private final Map<String, Integer> scores = new HashMap<>();

    public Student(int id, String name, String className) {
        super(id, name); // မိဘ class (Person) ရဲ့ constructor ကို ခေါ်
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    public void addScore(String subject, int score) {
        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("Score must be 0-100: " + score);
        }
        scores.put(subject, score);
    }

    public Map<String, Integer> getScores() {
        return scores;
    }

    public double getAverage() {
        if (scores.isEmpty()) {
            return 0.0;
        }
        int total = 0;
        for (int s : scores.values()) {
            total += s;
        }
        return (double) total / scores.size();
    }

    @Override
    public String getRole() {
        return "Student";
    }

    // override = မိဘရဲ့ method ကို ကိုယ်ပိုင်ပုံစံ ပြန်ရေး (polymorphism)
    @Override
    public String toDisplayString() {
        return super.toDisplayString()
                + " (" + className + ") avg=" + String.format("%.1f", getAverage());
    }
}
