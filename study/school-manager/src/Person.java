// abstract class = တိုက်ရိုက် new Person() လုပ်လို့မရ။ Student/Teacher ရဲ့ "မိဘ" class။
public abstract class Person implements Printable {
    // private = class အပြင်ကနေ တိုက်ရိုက် ပြင်လို့မရ (encapsulation)
    private final int id;
    private final String name;

    // constructor = object ဆောက်တဲ့အချိန် တန်ဖိုးစထည့်တဲ့နေရာ
    protected Person(int id, String name) {
        this.id = id;
        this.name = name;
    }

    // getter = private field ကို အပြင်က ဖတ်လို့ရအောင်
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    // abstract method = ကလေး class တိုင်း ကိုယ်ပိုင်ပုံစံနဲ့ ရေးရမယ်
    public abstract String getRole();

    @Override
    public String toDisplayString() {
        return "[" + getRole() + "] #" + id + " " + name;
    }
}
