-- Q1. INNER JOIN : ကျောင်းသားနာမည် + ဘာသာ + အမှတ်
SELECT st.name, sb.name AS subject, sc.score
FROM scores sc
JOIN students st ON st.id = sc.student_id
JOIN subjects sb ON sb.id = sc.subject_id
ORDER BY st.name, sb.name;

-- Q2. LEFT JOIN : အမှတ်မရှိသေးတဲ့ ကျောင်းသားကိုပါ ပြ (score က NULL)
SELECT st.name, sc.score
FROM students st
LEFT JOIN scores sc ON sc.student_id = st.id
WHERE sc.score IS NULL;

-- Q3. GROUP BY : ကျောင်းသားတစ်ယောက်ချင်း ပျမ်းမျှအမှတ်
SELECT st.name, ROUND(AVG(sc.score), 1) AS avg_score, COUNT(*) AS subjects
FROM students st
JOIN scores sc ON sc.student_id = st.id
GROUP BY st.id, st.name
ORDER BY avg_score DESC;

-- Q4. GROUP BY + HAVING : ပျမ်းမျှ 80 အထက် class တွေပဲ
SELECT st.class_name, ROUND(AVG(sc.score), 1) AS class_avg
FROM students st
JOIN scores sc ON sc.student_id = st.id
GROUP BY st.class_name
HAVING AVG(sc.score) >= 80;

-- Q5. Subquery : Java မှာ Java ပျမ်းမျှထက် ပိုရတဲ့သူ
SELECT st.name, sc.score
FROM scores sc
JOIN students st ON st.id = sc.student_id
WHERE sc.subject_id = (SELECT id FROM subjects WHERE name = 'Java')
  AND sc.score > (
      SELECT AVG(score) FROM scores
      WHERE subject_id = (SELECT id FROM subjects WHERE name = 'Java')
  );

-- Q6. Subquery with IN : Linux ယူထားတဲ့ ကျောင်းသားများ
SELECT name FROM students
WHERE id IN (
    SELECT student_id FROM scores
    WHERE subject_id = (SELECT id FROM subjects WHERE name = 'Linux')
);
