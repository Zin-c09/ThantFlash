-- テーブル作成 (table ဆောက်ခြင်း) + sample data
DROP TABLE IF EXISTS scores;
DROP TABLE IF EXISTS students;
DROP TABLE IF EXISTS subjects;

CREATE TABLE students (
    id         INTEGER PRIMARY KEY,
    name       TEXT NOT NULL,
    class_name TEXT NOT NULL
);

CREATE TABLE subjects (
    id   INTEGER PRIMARY KEY,
    name TEXT NOT NULL UNIQUE
);

-- scores က students နဲ့ subjects ကို ချိတ်ပေးတဲ့ table (FOREIGN KEY)
CREATE TABLE scores (
    student_id INTEGER NOT NULL REFERENCES students(id),
    subject_id INTEGER NOT NULL REFERENCES subjects(id),
    score      INTEGER NOT NULL CHECK (score BETWEEN 0 AND 100),
    PRIMARY KEY (student_id, subject_id)
);

INSERT INTO students (id, name, class_name) VALUES
    (101, 'Thant Zin', 'IT-1A'),
    (102, 'Aye Aye',   'IT-1A'),
    (103, 'Ken',       'IT-1B'),
    (104, 'Mika',      'IT-1B');

INSERT INTO subjects (id, name) VALUES
    (1, 'Java'),
    (2, 'SQL'),
    (3, 'Linux');

INSERT INTO scores (student_id, subject_id, score) VALUES
    (101, 1, 85), (101, 2, 78), (101, 3, 90),
    (102, 1, 92), (102, 2, 88),
    (103, 1, 70), (103, 3, 65);
-- Mika (104) မှာ အမှတ်မရှိသေး → LEFT JOIN လေ့ကျင့်ဖို့
