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
            li.className = "list-group-item d-flex justify-content-between align-items-start";
            li.dataset.id = task.id;
            li.dataset.deadline = task.deadline;

            const contentDiv = document.createElement('div');

            const title = document.createElement('h5');
            title.classList.toggle('task-title');
            title.textContent = task.name;
            contentDiv.appendChild(title);

            const small = document.createElement('small');
            small.className = "text-muted d-block mb-1";
            small.textContent = `Создано: ${new Date(task.created).toLocaleString()} ${task.deadline !== null ? `| Дедлайн: ${new Date(task.deadline).toLocaleString()}` : ""}`;
            contentDiv.appendChild(small);

            const badgeContainer = document.createElement('div');

            const statusBadge = document.createElement('span');
            statusBadge.className = "badge me-2";
            statusBadge.textContent = task.status;
            badgeContainer.appendChild(statusBadge);

            const priorityBadge = document.createElement('span');
            priorityBadge.className = "badge bg-info text-dark";
            priorityBadge.textContent = task.priority;
            badgeContainer.appendChild(priorityBadge);

            contentDiv.appendChild(badgeContainer);
            li.appendChild(contentDiv);

            const deleteBtn = document.createElement('button');
            deleteBtn.className = "btn btn-sm btn-outline-danger";
            deleteBtn.innerHTML = "&times;";
            deleteBtn.title = "Удалить задачу";
            deleteBtn.dataset.id = task.id;
            deleteBtn.addEventListener("click", async (e) => {
                const id = e.currentTarget.dataset.id;
                const urlDelete = `http://localhost:8080/tasks/${id}`;
                try {
                    const response = await fetch(urlDelete, {
                        method: 'DELETE',
                        headers: {
                            "Content-Type": "application/json"
                        }
                    });

                    const taskElement = document.querySelector(`[data-id="${id}"]`);
                    
                    taskElement.remove();
                }
                catch (err) {
                    console.error("Ошибка при удалении задачи");
                }
            });

            const btnDiv = document.createElement('div');
            btnDiv.className = 'd-flex flex-column gap-2';
            li.appendChild(btnDiv);

            const completeBtn = document.createElement('button');
            completeBtn.className = "btn btn-sm btn-success text-white";
            completeBtn.innerHTML = "✓";
            completeBtn.title = "Завершить задачу";
            completeBtn.dataset.id = task.id;


            btnDiv.appendChild(completeBtn);
            btnDiv.appendChild(deleteBtn);

            completeBtn.addEventListener('click', async (e) => {
                const id = e.currentTarget.dataset.id;
                const url = `http://localhost:8080/tasks/${id}`;

                try {
                    const response = await fetch(url, {
                        method: "PATCH",
                        headers: {
                            "Content-Type": "Application/json",
                        }
                    })

                    const task = await response.json();

                    const statusUpd = document.querySelector(`[data-id="${id}"] .badge`);
                    statusUpd.textContent = task.status;

                    colorTask(task, document.querySelector(`[data-id="${id}"]`));
                } catch(err) {
                    console.error("Ошибка при выставлении выполненной задачи");
                }
            });

            ul.appendChild(li);

            li.addEventListener('click', async (e) => {
                if (!e.target.closest('button') && e.target.tagName !== 'BUTTON') {
                    await showTaskDetailsModal(task.id);
                }
            });
            
        });

        parent.appendChild(ul);
        const lis = document.querySelectorAll('li');
        lis.forEach(element => {
            const task = {
                id: element.dataset.id,
                deadline: element.dataset.deadline === 'null' ? null : element.dataset.deadline
            };
            colorTask(task, element);
        });

    } catch (error) {
        console.error("Ошибка getAllTasks()");
    }
}

async function showTaskDetailsModal(id) {
    const url = new URL(`http://localhost:8080/tasks/${id}`);

    try {
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                "Content-Type": "application/json"
            }
        });

        const task = await response.json();

        const modalOverlay = document.createElement('div');
        modalOverlay.style.position = "fixed";
        modalOverlay.style.top = 0;
        modalOverlay.style.left = 0;
        modalOverlay.style.width = "100%";
        modalOverlay.style.height = "100%";
        modalOverlay.style.backgroundColor = "rgba(0,0,0,0.5)";
        modalOverlay.style.display = "flex";
        modalOverlay.style.justifyContent = "center";
        modalOverlay.style.alignItems = "center";
        modalOverlay.style.zIndex = "1000";

        const modalContent = document.createElement('div');
        modalContent.className = "bg-white p-4 rounded shadow";
        modalContent.style.width = "100%";
        modalContent.style.maxWidth = "600px";

        const modalHeader = document.createElement('h3');
        modalHeader.className = "mb-4 pb-2 border-bottom task-title";
        modalHeader.textContent = task.name;

        const editButton = document.createElement('button');
        editButton.className = "btn btn-sm btn-outline-primary float-end";
        editButton.textContent = "Редактировать";
        modalHeader.appendChild(editButton);

        const detailsList = document.createElement('dl');
        detailsList.className = "row";

        const editForm = document.createElement('form');
        editForm.style.display = 'none';

        const nameInput = document.createElement('input');
        nameInput.type = "text";
        nameInput.className = "form-control mb-2";
        nameInput.value = task.name;
        nameInput.required = true;

        const descInput = document.createElement('textarea');
        descInput.className = "form-control mb-2";
        descInput.value = task.description || "";

        const deadlineInput = document.createElement('input');
        deadlineInput.type = "date";
        deadlineInput.className = "form-control mb-2";
        if (task.deadline) {
            const deadlineDate = new Date(task.deadline);
            const year = deadlineDate.getFullYear();
            const month = String(deadlineDate.getMonth() + 1).padStart(2, '0');
            const day = String(deadlineDate.getDate()).padStart(2, '0');
            deadlineInput.value = `${year}-${month}-${day}`;
        }

        const prioritySelect = document.createElement('select');
        prioritySelect.className = "form-select mb-3";
        ["", "Low", "Medium", "High", "Critical"].forEach(level => {
            const opt = document.createElement("option");
            opt.value = level;
            opt.textContent = level;
            if (level === (task.priority || "")) {
                opt.selected = true;
            }
            prioritySelect.appendChild(opt);
        });

        const saveButton = document.createElement('button');
        saveButton.type = "submit";
        saveButton.className = "btn btn-primary me-2";
        saveButton.textContent = "Сохранить";

        const cancelEditButton = document.createElement('button');
        cancelEditButton.type = "button";
        cancelEditButton.className = "btn btn-secondary";
        cancelEditButton.textContent = "Отмена";

        const buttonGroup = document.createElement('div');
        buttonGroup.className = "d-flex mt-3";
        buttonGroup.appendChild(saveButton);
        buttonGroup.appendChild(cancelEditButton);

        editForm.appendChild(nameInput);
        editForm.appendChild(descInput);
        editForm.appendChild(deadlineInput);
        editForm.appendChild(prioritySelect);
        editForm.appendChild(buttonGroup);

        const toggleEditMode = (isEditing) => {
            if (isEditing) {
                detailsList.style.display = 'none';
                editForm.style.display = 'block';
                editButton.style.display = 'none';
            } else {
                detailsList.style.display = 'block';
                editForm.style.display = 'none';
                editButton.style.display = 'block';
            }
        };

        editButton.addEventListener('click', () => toggleEditMode(true));
        cancelEditButton.addEventListener('click', () => toggleEditMode(false));

        editForm.addEventListener('submit', async (e) => {
            e.preventDefault();
        
            const updatedTask = {
                name: nameInput.value.trim(),
                description: descInput.value.trim(),
                priority: prioritySelect.value === "" ? null : prioritySelect.value
            };
        
            if (deadlineInput.value) {
                const [year, month, day] = deadlineInput.value.split('-');
                updatedTask.deadline = `${year}-${month}-${day}T23:59:59Z`;
            } else {
                updatedTask.deadline = null;
            }
        
            try {
                const updateResponse = await fetch(url, {
                    method: "PUT",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify(updatedTask)
                });
        
                if (!updateResponse.ok) {
                    const errorData = await updateResponse.json();
                    
                    if (updateResponse.status === 400 && errorData.description) {
                        const errorDiv = document.createElement('div');
                        errorDiv.className = "alert alert-danger mt-3";
                        errorDiv.textContent = errorData.description;
                        
                        const existingError = editForm.querySelector('.alert-danger');
                        if (existingError) {
                            existingError.remove();
                        }
                        
                        editForm.appendChild(errorDiv);
                        return;
                    }
                    throw new Error(errorData.description || 'Ошибка при обновлении задачи');
                }
        
                const updatedTaskData = await updateResponse.json();
                
                modalHeader.textContent = updatedTaskData.name;
                modalHeader.appendChild(editButton);
                
                const existingError = editForm.querySelector('.alert-danger');
                if (existingError) {
                    existingError.remove();
                }
                
                const taskWrapper = document.querySelector('.task-wrapper');
                taskWrapper.innerHTML = "";
                await getAllTasks(taskWrapper);
                
                toggleEditMode(false);
            } catch (error) {
                console.error("Ошибка при обновлении задачи:", error);
                
                const errorDiv = document.createElement('div');
                errorDiv.className = "alert alert-danger mt-3";
                errorDiv.textContent = error.message || 'Не удалось обновить задачу';
                
                const existingError = editForm.querySelector('.alert-danger');
                if (existingError) {
                    existingError.remove();
                }
                
                editForm.appendChild(errorDiv);
            }
        });

        const addDetailRow = (label, value) => {
            if (value === null || value === undefined) return;

            const dt = document.createElement('dt');
            dt.className = "col-sm-4 text-muted fw-normal";
            dt.textContent = label;

            const dd = document.createElement('dd');
            dd.className = "col-sm-8 mb-3";
            
            if (label.includes("Дата") || label === "Дедлайн") {
                dd.textContent = value ? new Date(value).toLocaleString() : "-";
            } else {
                dd.textContent = value || "-";
            }

            detailsList.appendChild(dt);
            detailsList.appendChild(dd);
        };

        addDetailRow("ID задачи", task.id);
        addDetailRow("Статус", task.status);
        addDetailRow("Приоритет", task.priority);
        addDetailRow("Описание", task.description);
        addDetailRow("Дата создания", task.created);
        addDetailRow("Последнее изменение", task.edited);
        addDetailRow("Дедлайн", task.deadline);

        const closeButton = document.createElement('button');
        closeButton.className = "btn btn-outline-secondary mt-3 w-100";
        closeButton.textContent = "Закрыть";
        closeButton.addEventListener('click', () => {
            modalOverlay.remove();
        });

        modalContent.appendChild(modalHeader);
        modalContent.appendChild(detailsList);
        modalContent.appendChild(editForm);
        modalContent.appendChild(closeButton);
        modalOverlay.appendChild(modalContent);

        document.body.appendChild(modalOverlay);

        modalOverlay.addEventListener('click', (e) => {
            if (e.target === modalOverlay) {
                modalOverlay.remove();
            }
        });

    } catch (err) {
        console.error("Ошибка при просмотре задачи:", err);
        alert('Не удалось загрузить данные задачи');
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

    const showFormBtn = document.createElement('button');
    showFormBtn.className = "btn btn-success mb-3";
    showFormBtn.textContent = "Добавить задачу";
    container.appendChild(showFormBtn);

    const modalOverlay = document.createElement('div');
    modalOverlay.style.position = "fixed";
    modalOverlay.style.top = 0;
    modalOverlay.style.left = 0;
    modalOverlay.style.width = "100%";
    modalOverlay.style.height = "100%";
    modalOverlay.style.backgroundColor = "rgba(0,0,0,0.5)";
    modalOverlay.style.display = "none";
    modalOverlay.style.justifyContent = "center";
    modalOverlay.style.alignItems = "center";
    modalOverlay.style.zIndex = "1000";

    const formCard = document.createElement('div');
    formCard.className = "bg-white p-4 rounded shadow";
    formCard.style.width = "100%";
    formCard.style.maxWidth = "500px";

    const form = document.createElement('form');

    const nameInput = document.createElement('input');
    nameInput.type = "text";
    nameInput.className = "form-control mb-2";
    nameInput.placeholder = "Название задачи";
    nameInput.required = true;

    const descInput = document.createElement('textarea');
    descInput.className = "form-control mb-2";
    descInput.placeholder = "Описание";

    const deadlineInput = document.createElement('input');
    deadlineInput.type = "date";
    deadlineInput.className = "form-control mb-2";

    const maxDate = new Date();
    maxDate.setUTCFullYear(maxDate.getUTCFullYear() + 10);
    const maxDateISO = maxDate.toISOString().split('T')[0];

    const twoYearsAgo = new Date();
    twoYearsAgo.setUTCFullYear(twoYearsAgo.getUTCFullYear() - 2);
    const minDateISO = twoYearsAgo.toISOString().split('T')[0];

    deadlineInput.min = minDateISO;
    deadlineInput.max = maxDateISO;

    const prioritySelect = document.createElement('select');
    prioritySelect.className = "form-select mb-3";
    ["", "Low", "Medium", "High", "Critical"].forEach(level => {
        const opt = document.createElement("option");
        opt.value = level;
        opt.textContent = level;

        if (level === "") {
            opt.selected = true;
        }

        prioritySelect.appendChild(opt);
    });

    const buttonGroup = document.createElement('div');
    buttonGroup.className = "d-flex justify-content-between";

    const submitButton = document.createElement('button');
    submitButton.type = "submit";
    submitButton.className = "btn btn-primary";
    submitButton.textContent = "Создать";

    const cancelButton = document.createElement('button');
    cancelButton.type = "button";
    cancelButton.className = "btn btn-secondary";
    cancelButton.textContent = "Отмена";

    buttonGroup.appendChild(cancelButton);
    buttonGroup.appendChild(submitButton);

    form.appendChild(nameInput);
    form.appendChild(descInput);
    form.appendChild(deadlineInput);
    form.appendChild(prioritySelect);
    form.appendChild(buttonGroup);

    formCard.appendChild(form);
    modalOverlay.appendChild(formCard);
    document.body.appendChild(modalOverlay);

    cancelButton.addEventListener("click", () => {
        modalOverlay.style.display = "none";
    });

    showFormBtn.addEventListener("click", () => {
        modalOverlay.style.display = "flex";
    });

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

    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        const trimmedName = nameInput.value.trim();

        if (trimmedName.length < 4) {
            nameInput.setCustomValidity("Имя задачи должно содержать минимум 4 символа");
            nameInput.reportValidity();
            return;
        } else if (trimmedName.length > 255) {
            nameInput.setCustomValidity("Имя задачи должно содержать не более 255 символов");
            nameInput.reportValidity();
            return;
        } 
        else {
            nameInput.setCustomValidity("");
            nameInput.value = trimmedName;
        }

        const priorityMacros = [...trimmedName.matchAll(/!(\d)/g)];

        if (priorityMacros.length > 0 && prioritySelect.value === "") {
            const validMacro = priorityMacros.find(m => {
                const digit = parseInt(m[1]);
                return digit >= 1 && digit <= 4;
            });

            if (validMacro) {
                const macroDigit = validMacro[1];

                switch (macroDigit) {
                    case "1":
                        prioritySelect.value = "Critical";
                        break;
                    case "2":
                        prioritySelect.value = "High";
                        break;
                    case "3":
                        prioritySelect.value = "Medium";
                        break;
                    case "4":
                        prioritySelect.value = "Low";
                        break;
                }

                nameInput.value = trimmedName.replace(validMacro[0], "").trim();

                if (nameInput.value.length < 4) {
                    nameInput.setCustomValidity("Имя задачи должно содержать минимум 4 символа");
                    nameInput.reportValidity();
                    return;
                }
            }
        }

        
    
        let deadline = null;
        if (deadlineInput.value) {
            const deadlineDate = new Date(deadlineInput.value);
            deadlineDate.setUTCHours(23, 59, 59);
            deadline = deadlineDate.toISOString();
            console.log(`deadline = ${deadline}\ndeadlineDate = ${deadlineDate}`);
        }

        const newTask = {
            "name": nameInput.value.trim(),
            "description": descInput.value.trim(),
            "deadline": deadline,
            "priority": prioritySelect.value === "" ? null : prioritySelect.value
        };

        try {
            const response = await fetch("http://localhost:8080/tasks", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(newTask)
            });

            if (!response.ok) {
                const errorData = await response.json();
                
                if (response.status === 400 && errorData.description) {
                    nameInput.setCustomValidity(errorData.description);
                    nameInput.reportValidity();
                    return;
                }
                throw new Error(errorData.description || "Ошибка при создании задачи");
            }

            nameInput.value = "";
            descInput.value = "";
            deadlineInput.value = "";
            prioritySelect.selectedIndex = 0;

            modalOverlay.style.display = "none";
            taskWrapper.innerHTML = "";
            await getAllTasks(taskWrapper, sortSelect.value.split("-")[0], sortSelect.value.split("-")[1]);

        } catch (error) {
            console.error("Ошибка при отправке задачи: ", error);
            if (error.message) {
                nameInput.setCustomValidity(error.message);
                nameInput.reportValidity();
            }
        }
    });

    nameInput.addEventListener("input", () => {
        const trimmedName = nameInput.value.trim();
        if (trimmedName.length >= 4) {
            nameInput.setCustomValidity("");
        }
    });

    await getAllTasks(taskWrapper);
}
let lisTask;
firstLoad();

function colorTask(task, li) {
    const status = li.querySelector(`div div .badge`);

    const threeDays = 3 * 24 * 60 * 60 * 1000;
    const now = new Date().getTime(); 
    const deadline = new Date(task.deadline).getTime();
    const dayDiff = (deadline - now);//диапазон сегодня - дедлайн может быть либо меньше 3 и больше 0, либо больше 3, либо меньше 0

    const orange = status.textContent == "Active" && dayDiff > 0 && dayDiff < threeDays;
    const red = status.textContent == "Overdue" && dayDiff < 0;
    const white = (status.textContent == "Late" || status.textContent == "Completed");
    const whiteAndDnone = (dayDiff > threeDays) || task.deadline === null;
    if (orange) {
        li.classList.add("bg-orange");
        status.classList.add("d-none");
    } else if (red) {
        li.classList.add("bg-red");
        status.classList.add("d-none");
    } else if (white) {
        li.classList.remove("bg-red");
        li.classList.remove("bg-orange");
        status.textContent === "Completed" ? status.classList.add("bg-green") : status.classList.add("bg-dark-green");
        status.classList.remove("d-none");
    } else if (whiteAndDnone) {
        status.classList.add("d-none");
    }
}