function getAllTasks() {
    fetch("http://localhost:8080/tasks", {
        method: "GET",
        headers: {
            "Content-Type": "application/json"
        }
    })
    .then(response => response.json())
    .then(data => 
        data.forEach(element => {
            console.log(element);
        })

    )
    .catch(error => console.error("Ошибка getAllTasks()"));
}

getAllTasks();