## ▶️ How to Run in VM Emulator

1. Open the VM Emulator:

   * Go to `nand2tetris/tools/`
   * Run `VMEmulator.bat`

2. Load the program:

   * Click **File → Load Program**
   * Select the **project folder** (not individual `.vm` files)

   Example:

   ```
   ...\JACK-STRING-IMPLEMENTATION\Part1
   ```

3. Click **Open**

✔️ The emulator will automatically load all `.vm` files in the folder.

---

## ⚠️ Important Note

* Even though the option says **“Load Program”**, you must select the **entire folder**, not a single file.
* If you load only `Main.vm`, you may get errors like:

  ```
  Can't find MyString.vm
  ```
* Ensure all compiled `.vm` files (e.g., `Main.vm`, `MyString.vm`) are present in the same directory.

---

## ▶️ Run

* Click the **Run (▶️)** button
* Execution starts from `Main.main()`
