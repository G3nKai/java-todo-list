async function getAllTasks(parent) {
    try {
        const response = await fetch("http://localhost:8080/tasks", {
            method: "GET",
            headers: {
                "Content-Type": "application/json"
            }
        });
        const data = await response.json();

        const ul = document.createElement('ul');
        ul.className = "list-group shadow-sm";

        data.forEach(element => {

            const task = {
                "id": element.id,
                "name": element.name,
                "description": element.description,
                "deadline": element.deadline,
                "status": element.status,
                "priority": element.priority,
                "created": element.created,
                "edited": element.edited
            }

            const li = document.createElement('li');
            li.className = "list-group-item";
            li.textContent = task.name;
            ul.appendChild(li);
        });

        parent.appendChild(ul);
        
    } catch (error) {
        console.error("Ошибка getAllTasks()");
    }
}

async function firstLoad() {
    const body = document.querySelector('body');
    body.className = "bg-light";

    const div1 = document.createElement('div');
    div1.className = "container py-5";

    body.appendChild(div1);

    const mainH1 = document.createElement('h1');
    mainH1.className = "text-center mb-4";
    mainH1.textContent = "Мои задачи";
    div1.appendChild(mainH1);

    const div2 = document.createElement('div');
    div2.className = "task-wrapper";

    div1.appendChild(div2);

    await getAllTasks(div2);
    
}

firstLoad();