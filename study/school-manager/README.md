# School Manager — Java / SQL / Git / Linux လေ့ကျင့်ခန်း

ကျောင်းသား၊ ဆရာ၊ အမှတ်တွေကို စီမံတဲ့ project သေးသေးလေး။ ဒီ project တစ်ခုထဲမှာ
**Java OOP · SQL · Git · Linux** ၄ မျိုးလုံး လေ့ကျင့်လို့ရအောင် ရေးထားပါတယ်။

```
school-manager/
├── run.sh              ← Linux script (chmod လေ့ကျင့်ရန်)
├── src/
│   ├── Printable.java  ← interface
│   ├── Person.java     ← abstract class (မိဘ)
│   ├── Student.java    ← Person ကို 継承 (Map သုံး)
│   ├── Teacher.java    ← Person ကို 継承
│   ├── School.java     ← Collections (List / Set / Map)
│   └── Main.java       ← program စတဲ့နေရာ
└── sql/
    ├── schema.sql      ← table ဆောက် + data ထည့်
    └── queries.sql     ← JOIN / GROUP BY / subquery
```

---

## 1. Linux — run ကြည့်မယ်

```bash
cd study/school-manager   # folder ထဲဝင် (cd = change directory)
ls -l                     # ဖိုင်စာရင်း (-l = permission, size ပါပြ)
chmod +x run.sh           # run.sh ကို "run လို့ရ" (x = execute) permission ပေး
./run.sh                  # run
```

| Command | အဓိပ္ပာယ် | ဥပမာ |
| --- | --- | --- |
| `pwd` | ကိုယ်ဘယ် folder ထဲရောက်နေလဲ | `pwd` |
| `ls` / `ls -la` | ဖိုင်စာရင်း (`-a` = ဖျောက်ထားတဲ့ `.git` ပါပြ) | `ls -la` |
| `cd` | folder ပြောင်း (`..` = အပေါ်တစ်ဆင့်၊ `~` = home) | `cd ..` |
| `mkdir` | folder အသစ် | `mkdir test` |
| `cat` | ဖိုင်ထဲက စာကို ပြ | `cat run.sh` |
| `chmod` | permission ပြောင်း | `chmod 755 run.sh` |
| `ssh` | တခြားစက် (server) ထဲ ဝင် | `ssh thant@192.168.1.10` |
| `vim` | terminal ထဲက text editor | `vim src/Main.java` |

**chmod နံပါတ်** — `r=4, w=2, x=1` ပေါင်းတာ။ `755` = owner `rwx`(7) / group `r-x`(5) / others `r-x`(5)

**vim အသက်ရှင်ရေး** — `i` = စာရိုက်မုဒ် · `Esc` = ရပ် · `:w` = save · `:q` = ထွက် · `:wq` = save+ထွက် · `:q!` = မသိမ်းဘဲထွက်

---

## 2. Java OOP — ဖိုင်တစ်ခုချင်း

### `Printable.java` — interface
```java
public interface Printable {
    String toDisplayString();
}
```
"ဒီ class က `toDisplayString()` ရှိရမယ်" ဆိုတဲ့ **ကတိ** ပဲ။ body `{ }` မပါ။
`implements Printable` လုပ်တဲ့ class က ဒီ method ကို မဖြစ်မနေ ရေးရတယ်။

### `Person.java` — abstract class + encapsulation
- `abstract class` → `new Person(...)` တိုက်ရိုက်လုပ်လို့မရ။ ကလေး class တွေအတွက် **ပုံစံခွက်**။
- `private final int id;` → `private` = အပြင်က မထိရ၊ `final` = တစ်ခါထည့်ပြီးရင် မပြောင်းရ။
- `getId()` / `getName()` → **getter**။ private data ကို ဖတ်ခွင့်ပဲပေး (= **encapsulation / カプセル化**)။
- `public abstract String getRole();` → ကလေး class တိုင်း ကိုယ်ပိုင်အဖြေ ရေးရမယ်။

### `Student.java` — 継承 (inheritance)
- `class Student extends Person` → Person မှာရှိတာ အကုန် ရလာတယ်။
- `super(id, name);` → မိဘ constructor ကို ခေါ်။ **constructor ရဲ့ ပထမဆုံးစာကြောင်း** ဖြစ်ရမယ်။
- `Map<String, Integer> scores` → `"Java" → 85` လို **key → value** တွဲသိမ်း။
- `addScore()` မှာ `0–100` မဟုတ်ရင် `throw new IllegalArgumentException(...)` → data မှားမဝင်အောင် ကာကွယ်။
- `@Override toDisplayString()` → မိဘရဲ့ method ကို ပြန်ရေး။ `super.toDisplayString()` နဲ့ မိဘဗားရှင်းကို ယူသုံးပြီး ထပ်ဖြည့်။

### `Teacher.java`
Student နဲ့ ပုံစံတူ — `extends Person`၊ `getRole()` က `"Teacher"` ပြန်ပေး။

### `School.java` — Collections
| Type | ထူးခြားချက် | ဒီမှာ သုံးတဲ့နေရာ |
| --- | --- | --- |
| `List` (`ArrayList`) | အစဉ်လိုက်၊ ထပ်နေလည်းရ | `people` စာရင်း |
| `Set` (`TreeSet`) | **မထပ်ရ**၊ TreeSet က စီပေး | class နာမည်များ |
| `Map` (`HashMap`) | key → value | class → ကျောင်းသားစာရင်း |

- `p instanceof Student s` → `p` က Student ဟုတ်ရင် `s` ဆိုတဲ့နာမည်နဲ့ တန်းသုံး။
- `computeIfAbsent(key, k -> new ArrayList<>())` → key မရှိသေးရင် list အသစ်ဆောက်ပြီးမှ ထည့်။
- `Comparator.comparingDouble(Student::getAverage).reversed()` → ပျမ်းမျှနဲ့ စီ၊ `reversed()` = များရာက စ။

### `Main.java` — polymorphism (ポリモーフィズム)
```java
for (Person p : school.getPeople()) {
    System.out.println(p.toDisplayString());
}
```
`p` ရဲ့ type က `Person` ပဲ။ ဒါပေမဲ့ Java က အမှန်တကယ် Teacher လား Student လား
**run ချိန်မှာ သိပြီး** သက်ဆိုင်ရာ `toDisplayString()` ကို ခေါ်ပေးတယ်။ ဒါ OOP ရဲ့ အဓိကအချက်။

---

## 3. SQL

Table ၃ ခု — `students`, `subjects`, `scores`။ `scores` က ကျန် ၂ ခုကို **FOREIGN KEY** နဲ့ ချိတ်တယ်။

```bash
sqlite3 school.db < sql/schema.sql    # table ဆောက် + data ထည့်
sqlite3 school.db < sql/queries.sql   # query တွေ run
sqlite3 school.db                     # interactive (ထွက်ရင် .quit)
```
(`sqlite3` မရှိရင် Ubuntu: `sudo apt install sqlite3`)

| Query | သင်ခန်းစာ | ရလဒ် |
| --- | --- | --- |
| Q1 `JOIN` | table ၃ ခုကို `ON` နဲ့ ချိတ်။ ကိုက်တဲ့ row ပဲ ထွက် | ကျောင်းသား/ဘာသာ/အမှတ် ၇ ကြောင်း |
| Q2 `LEFT JOIN` | ဘယ်ဘက် table က row **အကုန်** ထွက်၊ မကိုက်ရင် `NULL` | Mika (အမှတ်မရှိ) |
| Q3 `GROUP BY` | ကျောင်းသားအလိုက် စုပြီး `AVG()`, `COUNT()` | Aye Aye 90.0 / Thant Zin 84.3 / Ken 67.5 |
| Q4 `HAVING` | group လုပ်ပြီး**နောက်**မှ စစ် (`WHERE` က group မလုပ်ခင်) | IT-1A 86.6 |
| Q5 subquery | `( SELECT AVG ... )` အတွင်းက အရင် run | Java ပျမ်းမျှ 82.3 ထက်များသူ |
| Q6 `IN` subquery | subquery ရလဒ် စာရင်းထဲ ပါသလား | Thant Zin, Ken |

**SQL run တဲ့ အစဉ်** (မှတ်ထားပါ): `FROM → JOIN → WHERE → GROUP BY → HAVING → SELECT → ORDER BY`

Java နဲ့ ယှဉ်ကြည့်ပါ — `School.groupByClass()` ≈ `GROUP BY class_name`၊ `rankByAverage()` ≈ Q3 ။

---

## 4. Git / GitHub

```bash
git status                      # ဘာပြောင်းထားလဲ
git add src/Main.java           # commit ထဲ ထည့်မယ့်ဖိုင် ရွေး (staging)
git commit -m "Add new student" # မှတ်တမ်းတင် (save point)
git log --oneline               # commit history
git push origin <branch>        # GitHub ပေါ် တင်
git pull                        # GitHub ကနေ နောက်ဆုံးဗားရှင်း ဆွဲ
git checkout -b homework-1      # branch အသစ်ဖွဲ့ပြီး ပြောင်း
git diff                        # မ commit ရသေးတဲ့ အပြောင်းအလဲ ကြည့်
```

**စီးဆင်းပုံ:** `ဖိုင်ပြင် → git add → git commit → git push`

`.gitignore` မှာ `out/` (compile ထွက်ဖိုင်) နဲ့ `*.db` ကို ထည့်ထားတယ် — ထုတ်လုပ်လို့ရတဲ့ဖိုင်တွေ Git ထဲ မထည့်ရ။

---

## 5. လေ့ကျင့်ခန်း (宿題)

1. **Java** — `Staff extends Person` class အသစ်ရေး (`department` field)။ `Main` မှာ ထည့်ပြီး run။
2. **Java** — `School` မှာ `findByName(String name)` method ထည့် (`for` loop + `equals`)။
3. **Java** — `Student.getMaxScore()` ရေး (အမြင့်ဆုံးအမှတ်)။
4. **SQL** — ဘာသာရပ်တစ်ခုချင်းရဲ့ ပျမ်းမျှ (`subjects` JOIN `scores` GROUP BY)။
5. **SQL** — Mika ကို `INSERT INTO scores` နဲ့ အမှတ်ထည့်၊ Q2 ပြန် run ကြည့်။
6. **SQL** — အမှတ်အများဆုံးရတဲ့ ကျောင်းသား (subquery + `MAX`)။
7. **Linux** — `chmod 644 run.sh` လုပ်ပြီး `./run.sh` → ဘာ error တက်လဲ? ဘာကြောင့်လဲ?
8. **Git** — လေ့ကျင့်ခန်းတစ်ခုပြီးတိုင်း commit တစ်ခါ၊ ပြီးရင် push။
