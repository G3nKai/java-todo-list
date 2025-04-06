async function getAllTasks(parent, field = '', direction = '') {
    try {
        const url = new URL("http://localhost:8080/tasks");
        
        if (field && direction) {
            url.searchParams.append("sortBy", field);
            url.searchParams.append("direction", direction);
        }

        const response = await fetch(url, {
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
            small.textContent = `Создано: ${new Date(task.created).toLocaleString()} ${task.deadline !== null ? `| Дедлайн: ${new Date(task.deadline).toLocaleString()}` : ""}`;
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

    const container = document.createElement('div');
    container.className = "container py-5";

    body.appendChild(container);

    const header = document.createElement('h1');
    header.className = "text-center mb-4";
    header.textContent = "Мои задачи";
    container.appendChild(header);

    const selectWrapper = document.createElement('div');
    selectWrapper.className = "mb-3";

    const sortSelect = document.createElement('select');
    sortSelect.className = "form-select w-auto";

    const options = [
        { text: "Сортировать по...", value: "" },
        { text: "Дата создания ↑", value: "created-asc" },
        { text: "Дата создания ↓", value: "created-desc" },
        { text: "Статус задачи ↑", value: "status-asc" },
        { text: "Статус задачи ↓", value: "status-desc" },
        { text: "Приоритет ↑", value: "priority-asc" },
        { text: "Приоритет ↓", value: "priority-desc" }
    ];

    options.forEach(opt => {
        const option = document.createElement("option");
        option.value = opt.value;
        option.textContent = opt.text;
        sortSelect.appendChild(option);
    });

    selectWrapper.appendChild(sortSelect);
    container.appendChild(selectWrapper);

    const taskWrapper = document.createElement('div');
    taskWrapper.className = "task-wrapper";
    container.appendChild(taskWrapper);

    sortSelect.addEventListener("change", async (e) => {
        const [field, direction] = e.target.value.split("-");
        taskWrapper.innerHTML = "";
        await getAllTasks(taskWrapper, field, direction);
    });

    await getAllTasks(taskWrapper);
}

firstLoad();