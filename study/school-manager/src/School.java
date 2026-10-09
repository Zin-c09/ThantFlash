import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

// Collections (List / Map / Set) တွေကို စုသုံးထားတဲ့ class
public class School {
    // List = အစဉ်လိုက် စာရင်း၊ ထပ်နေလည်းရ
    private final List<Person> people = new ArrayList<>();

    public void add(Person p) {
        people.add(p);
    }

    public List<Person> getPeople() {
        return people;
    }

    // instanceof နဲ့ Student တွေကိုပဲ စစ်ထုတ်
    public List<Student> getStudents() {
        List<Student> result = new ArrayList<>();
        for (Person p : people) {
            if (p instanceof Student s) {
                result.add(s);
            }
        }
        return result;
    }

    // Set = မထပ်ရ။ TreeSet က အက္ခရာစဉ်အတိုင်း စီပေး
    public Set<String> getClassNames() {
        Set<String> names = new TreeSet<>();
        for (Student s : getStudents()) {
            names.add(s.getClassName());
        }
        return names;
    }

    // SQL ရဲ့ GROUP BY နဲ့ တူတူပဲ — class အလိုက် ကျောင်းသားတွေ စု
    public Map<String, List<Student>> groupByClass() {
        Map<String, List<Student>> groups = new HashMap<>();
        for (Student s : getStudents()) {
            groups.computeIfAbsent(s.getClassName(), k -> new ArrayList<>()).add(s);
        }
        return groups;
    }

    // Comparator နဲ့ ပျမ်းမျှအမှတ် များရာကနေ နည်းရာ စီ
    public List<Student> rankByAverage() {
        List<Student> sorted = new ArrayList<>(getStudents());
        sorted.sort(Comparator.comparingDouble(Student::getAverage).reversed());
        return sorted;
    }
}
