# 🪟 Windows Setup Guide for JIHLL

Welcome to the JIHLL setup guide for Windows users.

## Prerequisites

You must have the Java Development Kit (JDK) installed (version 11 or higher).

### 1. Check for Java
Open Command Prompt (cmd) or PowerShell and type:
```cmd
java -version
```

### 2. Install Java (if not installed)
Download and install the latest JDK from:
- [Eclipse Adoptium (Temurin)](https://adoptium.net/) (Recommended)
- [Oracle Java Downloads](https://www.oracle.com/java/technologies/downloads/)

*Ensure you check "Add to PATH" during installation.*

## Compilation

1. **Open Command Prompt** or PowerShell.

2. **Navigate to the `src` directory** inside the project folder:
   ```cmd
   cd C:\path\to\FocusNexus\src
   ```

3. **Compile the code:**
   ```cmd
   javac com\jihll\*.java
   ```

## Verification

Run the interactive shell:
```cmd
java com.jihll.JIHLLLanguage
```

If successful, you will see:
```text
JIHLL Interactive Shell (Type 'exit' to quit)
------------------------------------------
> 
```

Success! Check [README_RUNNING.md](README_RUNNING.md) to start coding.
