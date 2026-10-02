public class Teacher extends Person {
    private final String subject;

    public Teacher(int id, String name, String subject) {
        super(id, name);
        this.subject = subject;
    }

    public String getSubject() {
        return subject;
    }

    @Override
    public String getRole() {
        return "Teacher";
    }

    @Override
    public String toDisplayString() {
        return super.toDisplayString() + " teaches " + subject;
    }
}
