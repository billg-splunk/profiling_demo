const list = document.querySelector("#scenario-list");
const template = document.querySelector("#scenario-template");
const connectionDot = document.querySelector("#connection-dot");
const connectionStatus = document.querySelector("#connection-status");
const cards = new Map();

function humanize(key) {
    return key
        .replace(/([a-z])([A-Z])/g, "$1 $2")
        .replace(/^./, (character) => character.toUpperCase());
}

function formatValue(key, value) {
    if (key.toLowerCase().includes("megabytes")) {
        return `${value} MiB`;
    }
    if (key.toLowerCase().includes("millis")) {
        return `${value} ms`;
    }
    return String(value);
}

function renderDetails(container, details) {
    container.replaceChildren();
    Object.entries(details).forEach(([key, value]) => {
        const row = document.createElement("div");
        const term = document.createElement("dt");
        const definition = document.createElement("dd");
        term.textContent = humanize(key);
        definition.textContent = formatValue(key, value);
        row.append(term, definition);
        container.append(row);
    });
}

function updateCard(card, scenario) {
    card.element.classList.toggle("active", scenario.enabled);
    card.state.textContent = scenario.enabled ? "Scenario active" : "Normal";
    card.input.checked = scenario.enabled;
    card.input.setAttribute("aria-label", `${scenario.enabled ? "Disable" : "Enable"} ${scenario.displayName}`);
    renderDetails(card.details, scenario.details);
}

function createCard(scenario) {
    const fragment = template.content.cloneNode(true);
    const element = fragment.querySelector(".scenario-card");
    const title = fragment.querySelector("h2");
    const description = fragment.querySelector(".description");
    const state = fragment.querySelector(".scenario-state");
    const input = fragment.querySelector("input");
    const details = fragment.querySelector(".details");
    const error = fragment.querySelector(".card-error");

    title.textContent = scenario.displayName;
    description.textContent = scenario.description;
    input.addEventListener("change", async () => {
        input.disabled = true;
        error.textContent = "";
        try {
            const response = await fetch(`/api/scenarios/${scenario.id}`, {
                method: "PUT",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify({enabled: input.checked})
            });
            const body = await response.json();
            if (!response.ok) {
                throw new Error(body.message || `Request failed with status ${response.status}`);
            }
            updateCard(cards.get(scenario.id), body);
        } catch (requestError) {
            input.checked = !input.checked;
            error.textContent = requestError.message;
        } finally {
            input.disabled = false;
        }
    });

    const card = {element, state, input, details, error};
    cards.set(scenario.id, card);
    updateCard(card, scenario);
    list.append(fragment);
}

async function refresh() {
    try {
        const response = await fetch("/api/scenarios", {cache: "no-store"});
        if (!response.ok) {
            throw new Error(`Application returned ${response.status}`);
        }
        const scenarios = await response.json();
        scenarios.forEach((scenario) => {
            const card = cards.get(scenario.id);
            if (card) {
                updateCard(card, scenario);
            } else {
                createCard(scenario);
            }
        });
        connectionDot.className = "status-dot connected";
        connectionStatus.textContent = "Application connected";
    } catch (error) {
        connectionDot.className = "status-dot disconnected";
        connectionStatus.textContent = `Disconnected: ${error.message}`;
    }
}

refresh();
setInterval(refresh, 1_000);
