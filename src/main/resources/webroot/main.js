document.addEventListener("DOMContentLoaded", () => {
  const form = document.getElementById("uploadForm");
  const fileInput = document.getElementById("file-input");
  const progressContainer = document.getElementById("progress-container");
  const statusDiv = document.getElementById("status-message");

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    const files = fileInput.files;
    if (files.length === 0) {
      showAlert("Please select at least one file.", "danger");
      return;
    }
    const formData = new FormData();
    for (let i = 0; i < files.length; i++) {
      formData.append("files", files[i]);
    }

    progressContainer.classList.remove("d-none");
    statusDiv.classList.add("d-none");

    try {
      const response = await fetch("/upload", {
        method: "POST",
        body: formData

      });


      progressContainer.classList.add("d-none");

      if (response.ok) {
        const result = await response.text();
        showAlert(`Upload Successful! Server says: ${result}`, "success");

        form.reset();
      } else {
        showAlert(`Upload Failed: ${response.statusText}`, "danger");
      }

    } catch (error) {

      progressContainer.classList.add("d-none");
      console.error("Error:", error);
      showAlert("Network Error: Could not reach the server.", "danger");
    }
  });

  function showAlert(message, type) {
    statusDiv.classList.remove("d-none", "alert-success", "alert-danger"); // Reset classes
    statusDiv.classList.add(`alert-${type}`); // Add specific color (success/danger)
    statusDiv.innerText = message;
  }
});
