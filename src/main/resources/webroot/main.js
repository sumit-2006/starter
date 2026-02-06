  document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("uploadForm");
    const fileInput = document.getElementById("file-input");
    const statusContainer = document.getElementById("status-container"); // Create this div in HTML

    form.addEventListener("submit", async (e) => {
      e.preventDefault();
      const files = fileInput.files;
      if (files.length === 0) return;

      const formData = new FormData();
      for (let i = 0; i < files.length; i++) {
        formData.append("files", files[i]);
      }

      try {
        const response = await fetch("/upload", { method: "POST", body: formData });

        if (response.ok) {
          const fileList = await response.json();


          statusContainer.innerHTML = "";
          fileList.forEach(file => createProgressBar(file));

          fileList.forEach(file => startPolling(file.id));
        }
      } catch (error) {
        console.error("Upload failed", error);
      }
    });

    function createProgressBar(file) {
      const html = `
        <div class="mb-3" id="file-box-${file.id}">
          <strong>${file.fileName}</strong> <span class="badge bg-secondary" id="status-${file.id}">PENDING</span>
          <div class="progress mt-1">
            <div id="bar-${file.id}" class="progress-bar progress-bar-striped progress-bar-animated" style="width: 0%">0</div>
          </div>
          <small class="text-muted">Success: <span id="success-${file.id}">0</span> | Failed: <span id="fail-${file.id}">0</span></small>
        </div>`;
      statusContainer.insertAdjacentHTML('beforeend', html);
    }

    function startPolling(id) {
      const interval = setInterval(async () => {
        try {
          const res = await fetch(`/status/${id}`);
          const data = await res.json();

          const statusBadge = document.getElementById(`status-${id}`);
          document.getElementById(`success-${id}`).innerText = data.successRecords;
          document.getElementById(`fail-${id}`).innerText = data.failureRecords;
          statusBadge.innerText = data.status;

          const bar = document.getElementById(`bar-${id}`);


          let pct = 0;
          if (data.totalRecords && data.totalRecords > 0) {
            const processed = data.successRecords + data.failureRecords;
            pct = Math.round((processed / data.totalRecords) * 100);
          }

          if (data.status === 'PROCESSING') {
            bar.style.width = `${pct}%`;
            bar.innerText = `${pct}%`; // Show the number inside the bar
            bar.classList.remove("bg-success", "bg-danger");

            statusBadge.className = "badge bg-primary";

          } else if (data.status === 'COMPLETED' || data.status === 'PARTIAL_SUCCESS') {
            bar.style.width = "100%";
            bar.innerText = "Done";

            bar.classList.add("bg-success");
            bar.classList.remove("progress-bar-animated", "progress-bar-striped");
            statusBadge.className = "badge bg-success";

            clearInterval(interval);

          } else if (data.status === 'FAILED') {
            bar.style.width = "100%";
            bar.innerText = "Failed";

            bar.classList.add("bg-danger");
            bar.classList.remove("progress-bar-animated", "progress-bar-striped");
            statusBadge.className = "badge bg-danger";

            clearInterval(interval);
          }

        } catch (e) {
          console.error("Polling error", e);
          clearInterval(interval);
        }
      }, 1000);
    }
  });
