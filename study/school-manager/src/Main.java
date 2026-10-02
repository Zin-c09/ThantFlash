import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        School school = new School();

        // Teacher နဲ့ Student နှစ်မျိုးလုံး Person ဖြစ်လို့ List<Person> ထဲ ထည့်လို့ရ
        school.add(new Teacher(1, "Tanaka", "Java"));
        school.add(new Teacher(2, "Suzuki", "SQL"));

        Student thant = new Student(101, "Thant Zin", "IT-1A");
        thant.addScore("Java", 85);
        thant.addScore("SQL", 78);
        thant.addScore("Linux", 90);

        Student aye = new Student(102, "Aye Aye", "IT-1A");
        aye.addScore("Java", 92);
        aye.addScore("SQL", 88);

        Student ken = new Student(103, "Ken", "IT-1B");
        ken.addScore("Java", 70);
        ken.addScore("Linux", 65);

        school.add(thant);
        school.add(aye);
        school.add(ken);

        System.out.println("=== All people (polymorphism) ===");
        for (Person p : school.getPeople()) {
            // p က Teacher လား Student လား Java က သူ့ဘာသာ သိပြီး မှန်တဲ့ method ကိုခေါ်
            System.out.println(p.toDisplayString());
        }

        System.out.println();
        System.out.println("=== Classes (Set) ===");
        System.out.println(school.getClassNames());

        System.out.println();
        System.out.println("=== Group by class (Map) ===");
        for (Map.Entry<String, List<Student>> e : school.groupByClass().entrySet()) {
            System.out.println(e.getKey() + " -> " + e.getValue().size() + " students");
        }

        System.out.println();
        System.out.println("=== Ranking (sort) ===");
        int rank = 1;
        for (Student s : school.rankByAverage()) {
            System.out.printf("%d. %s %.1f%n", rank++, s.getName(), s.getAverage());
        }

        System.out.println();
        System.out.println("=== Error handling ===");
        try {
            ken.addScore("SQL", 150);
        } catch (IllegalArgumentException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }
}
