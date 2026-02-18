# 🧭 Development Guidelines

## 🌿 Branching Strategies

The repository adopts a lightweight branching model suitable for small teams while maintaining code stability and structured development.

### **Branch Types**

- **`main`** — Contains stable, production-ready code.  
- **`feat/*`** — Used for developing new features.  
- **`bugfix/*`** — Used for fixing bugs or applying hotfixes.

### **Notes**
- Always create new branches from the latest `main` branch.  
- Merge completed features or fixes back into `main` through a pull request.  
- Keep branch names short and descriptive (e.g., `feat/login-api`, `bugfix/token-expiry`).
- Direct push to main branch is prohibited

## 🗂️ Backlog Strategies

To maintain consistency and traceability between backend (Function) and frontend (Apps) development, backlog items must follow a clear and standardized naming format.

### **Naming Format** 
UCXX[F/A] - (feat-name)


### **Description**
| Component | Meaning | Example |
|------------|----------|----------|
| **UC** | Abbreviation for *Use Case* | `UC01` |
| **XX** | Incremental number of the backlog item | `01`, `02`, `03`, ... |
| **F/A** | Type of feature development: <br>• **F** = Function (backend) <br>• **A** = Apps (frontend) | `F`, `A` |
| **feat-name** | Short descriptive name of the feature | `Login`, `Token System`, `User Profile` |

> If `F/A` is not defined, it means the backlog item involves both Function and Apps aspects.

---

### **Naming Rules**
1. Each **Use Case (UC)** should have a unique incremental number (`UC01`, `UC02`, etc.).  
2. For consistency, each `UCXXF` **must have a corresponding** `UCXXA`.  
   - Example: if `UC01F` exists for backend logic, there must be a `UC01A` for its frontend counterpart.  
3. The `feat-name` should be concise, descriptive, and use **Pascal Case** or **Title Case** for readability.  
4. Avoid special characters or unnecessary punctuation in feature names.
5. Direct push to `main` branch is prohibited.

---

### **Examples**

| Backlog ID | Type | Description |
|-------------|------|-------------|
| `UC01F - Login API` | Function | Backend implementation of user login endpoint. |
| `UC01A - Login Page` | Apps | Frontend page and form for user login. |
| `UC02F - Token System` | Function | Backend implementation for access and refresh tokens. |
| `UC02A - Token Handling` | Apps | Frontend logic for token storage and refresh flow. |
| `UC03 - User Profile` | Combined | Involves both backend and frontend development. |

---

### **Recommended Workflow**
1. Create backlog item(s) following the naming format.  
2. Create a corresponding branch based on the backlog ID.  
   - Example:  
     ```
     feat/UC01F-login-api
     feat/UC01A-login-page
     ```  
3. Develop and test changes on the feature branch.  
4. Open a pull request to merge into `main` once completed and reviewed.
# Skripsi-API
