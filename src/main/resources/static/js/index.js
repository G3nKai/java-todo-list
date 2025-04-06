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
            
            const title = document.createElement('h5');
            title.className = "mb-1 d-inline";
            title.textContent = task.name;
            li.appendChild(title);

            if (task.edited) {
                const editedBadge = document.createElement('span');
                editedBadge.className = "badge bg-warning text-dark ms-2";
                editedBadge.textContent = task.edited;
                title.appendChild(editedBadge);
            }

            const small = document.createElement('small');
            small.className = "text-muted d-block mb-1";
            small.textContent = `Создано: ${new Date(task.created).toLocaleString()} | Дедлайн: ${new Date(task.deadline).toLocaleString()}`;
            li.appendChild(small);

            const badgeContainer = document.createElement('div');

            const statusBadge = document.createElement('span');
            statusBadge.className = "badge bg-secondary me-2";
            statusBadge.textContent = task.status;
            badgeContainer.appendChild(statusBadge);

            const priorityBadge = document.createElement('span');
            priorityBadge.className = "badge bg-info text-dark";
            priorityBadge.textContent = task.priority;
            badgeContainer.appendChild(priorityBadge);

            li.appendChild(badgeContainer);


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