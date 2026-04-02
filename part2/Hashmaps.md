# 🧠 HashMap Usage in Hack Assembler

## 📌 What is a HashMap?

A **HashMap** in Java is a data structure that stores data in **key → value pairs**.

👉 Example:

```java
"LOOP" → 4
"i"    → 16
```

* Key = name (symbol)
* Value = address (memory location)

---

## 🎯 Why HashMap is Used in This Project?

In the assembler, we need to store and quickly retrieve:

* Labels
* Variables
* Predefined symbols

👉 This is exactly what a HashMap does efficiently.

---

## 🧠 Where HashMap is Used

### 🔹 1. Symbol Table

The HashMap acts as a **symbol table**, which stores:

```text
symbol → memory address
```

Example:

```java
symbolTable.put("LOOP", 4);
symbolTable.put("i", 16);
```

---

### 🔹 2. Label Handling (Pass 1)

When a label is found:

```asm
(LOOP)
```

We store:

```java
symbolTable.put("LOOP", romAddress);
```

👉 Later, when we see:

```asm
@LOOP
```

We retrieve:

```java
int value = symbolTable.get("LOOP");
```

---

### 🔹 3. Variable Handling

If a variable is used:

```asm
@i
```

And it is not already in the table:

```java
if (!symbolTable.containsKey("i")) {
    symbolTable.put("i", 16); // next available RAM address
}
```

---

### 🔹 4. Instruction Mapping

HashMaps are also used for instruction translation:

#### comp table:

```java
comp.put("D+A", "0000010");
```

#### dest table:

```java
dest.put("D", "010");
```

#### jump table:

```java
jump.put("JMP", "111");
```

👉 These allow quick conversion of assembly instructions into binary.

---

## ⚙️ Basic HashMap Operations Used

### ✔️ Insert (put)

```java
map.put("key", value);
```

### ✔️ Retrieve (get)

```java
map.get("key");
```

### ✔️ Check existence

```java
map.containsKey("key");
```

---

## 🧠 Why HashMap is Suitable

| Feature      | Benefit        |
| ------------ | -------------- |
| Fast lookup  | O(1) time      |
| Dynamic size | No fixed limit |
| Easy to use  | Simple syntax  |

---

## 🧠 Simple Explanation (for beginners)

👉 HashMap works like a dictionary:

```text
Word → Meaning
```

In this project:

```text
Symbol → Address
```

---

## 🧠 Viva Explanation

👉 You can say:

> “I used a HashMap to implement the symbol table. It stores labels and variables as keys and their memory addresses as values, allowing fast lookup during assembly.”

---

## 📌 Summary

* HashMap is used to store symbols and instruction mappings
* It enables fast and efficient translation
* It is a key component in building the assembler
