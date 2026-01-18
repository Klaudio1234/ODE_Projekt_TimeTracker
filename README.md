# ODE_Projekt_TimeTracker

The **TimeTracker** is a Java application that allows employees to record their working hours.  
Each employee can create different tasks and log time spent working on them.  
A time entry is created by starting a timer, working, and then stopping the timer.  
The recorded time is assigned to a task and saved.

The application consists of:

- a **Client**, which provides the user interface  
- a **Server**, which acts as the central data storage  

The client sends all recorded working times to the server via a **TCP connection**,  
where the server stores the data persistently.

---

## Team

- **Klaudio Shkalla** (ic25b004@technikum-wien.at)  
- **Erind Vora** (ic25b012@technikum-wien.at)  
- **Klevis Gjergjaj** (ic25b010@technikum-wien.at)

---
## Milestones

| Name            | Feature / Task                     | Detail                                              | Hrs | Done | Comment |
|-----------------|------------------------------------|-----------------------------------------------------|-----|------|---------|
| Klaudio Shkalla | Defined Must-Have Features         |                                                     | 0.5 | yes  |         |
| Erind Vora      | Defined Must-Have Features         |                                                     | 0.5 | yes  |         |
| Klevis Gjergjaj | Defined Must-Have Features         |                                                     | 0.5 | yes  |         |
| Erind Vora      | Defined Should-Have Features       |                                                     | 1   | yes  |         |
| Klaudio Shkalla | Defined Nice-to-Have Features      |                                                     | 1   | yes  |         |
| Klevis Gjergjaj | Defined Overkill Features          |                                                     | 1   | yes  |         |
| Klaudio Shkalla | Created Repository                 |                                                     | 0.5 | yes  |         |
| Klaudio Shkalla | Updated README                     |                                                     | 0.5 | yes  |         |
| Erind Vora      | Defined Package Structure          |                                                     | 1   | yes  |         |
| Klevis Gjergjaj | Planned Required Classes           |                                                     | 1   | yes  |         |
| Klaudio Shkalla | Created Empty Project              | Initial project pushed to repository                | 0.5 | yes  |         |
| All             | Cloned Repository                  |                                                     | 0.5 | yes  |         |
| Klaudio Shkalla | Added Model Package Classes        |                                                     | 0.5 | yes  |         |
| Erind Vora      | Added Storage & Network Classes    |                                                     | 0.5 | yes  |         |
| Klevis Gjergjaj | Added Exception Classes            |                                                     | 0.5 | yes  |         |
| Klevis Gjergjaj | Completed Model Package            | All model classes implemented                       | 0.2 | no   |         |
| Erind Vora      | Completed UI Package               | Timer and task UI classes                           | 0.2 | no   |         |
| Klaudio Shkalla | Completed UI & Network Main        | Main classes implemented                            | 0.2 | no   |         |
| Klaudio Shkalla | Implemented Task Base Class        | Abstract base class                                 | 1   | no   |         |
| Erind Vora      | Implemented Task Subclasses        | Design, Programming, Accounting                     | 1   | no   |         |
| Klevis Gjergjaj | Implemented Exception Classes      | NetworkException, StorageException                  | 1   | no   |         |
| Klaudio Shkalla | Implemented TimeEntry Class        |                                                     | 2   | no   |         |
| Erind Vora      | Implemented TimeManager Class      |                                                     | 2   | no   |         |
| Klevis Gjergjaj | Implemented FileStorage Class      |                                                     | 2   | no   |         |
| Klevis Gjergjaj | Implemented Client Class           |                                                     | 2   | yes  |         |
| Klaudio Shkalla | Implemented Server Class           |                                                     | 2   | yes  |         |
| Erind Vora      | Implemented Server Main            |                                                     | 2   | yes  |         |
| Klevis Gjergjaj | Implemented ClientLogger Class     |                                                     | 2   | yes  |         |
| Erind Vora      | Implemented AdminAuth Class        |                                                     | 2   | yes  |         |
| Klaudio Shkalla | Implemented TaskListController     |                                                     | 2   | yes  |         |
| Erind Vora      | Implemented TimerController        | Basics, safe, showError, formatHMS, stop            | 1.5 | yes  |         |
| Klaudio Shkalla | Implemented TimerController        | Refresh, start, build                               | 1.5 | yes  |         |
| Erind Vora      | Implemented TaskEditorController   | Constructor, build                                  | 2   | yes  |         |
| Klaudio Shkalla | Implemented TaskEditorController   | Clear task, fields, showError                       | 2   | yes  |         |
| Klaudio Shkalla | Implemented MainController         | Basic UI, features                                  | 2   | yes  |         |
| Erind Vora      | Implemented MainController         | Features                                            | 2   | yes  |         |
| Klaudio Shkalla | Changed Time Format in Controller  |                                                     | 0.2 | yes  |         |
| Klevis Gjergjaj | Added isConnected() Method         |                                                     | 0.5 | yes  |         |
| Klevis Gjergjaj | Added Client Sync Changes          |                                                     | 0.5 | yes  |         |
| Erind Vora      | Added Synchronization Changes      |                                                     | 0.5 | yes  |         |

---
