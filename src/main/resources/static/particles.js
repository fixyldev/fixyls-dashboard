const canvas = document.querySelector("#particles");
const ctx = canvas.getContext("2d");
const particles = [];
const mouse = { x: 0, y: 0, present: false };
const count = 25;
const connectDistance = 150;
const minSpeedMultiplier = 0.2;

canvas.width = window.innerWidth;
canvas.height = window.innerHeight;

addEventListener("pointermove", function (e) {
    mouse.x = e.pageX;
    mouse.y = e.pageY;

    if (e.pointerType == "mouse") {
        mouse.present = true;
    } else {
        mouse.present = false;
    }
});

addEventListener("pointerout", function () {
    mouse.present = false;
});

function Particle() {
    this.x = Math.random() * canvas.width;
    this.y = Math.random() * canvas.height;
    this.size = Math.random() + 0.5;
    this.speedX = Math.random() * 2 - 1;
    this.speedY = Math.random() * 2 - 1;
}

Particle.prototype.mouseDistance = function () {
    let diffX = this.x - mouse.x;
    let diffY = this.y - mouse.y;

    return Math.hypot(diffX, diffY);
}

Particle.prototype.move = function () {
    let multiplier = this.mouseDistance() / connectDistance;

    if (multiplier < minSpeedMultiplier && mouse.present === true) {
        multiplier = minSpeedMultiplier;
    } else if (multiplier > 1 || mouse.present === false) {
        multiplier = 1;
    }

    this.x += multiplier * this.speedX;
    this.y += multiplier * this.speedY;
}

Particle.prototype.teleport = function () {
    if (this.x < 0) {
        this.x = canvas.width;
    } else if (this.x > canvas.width) {
        this.x = 0;
    }

    if (this.y < 0) {
        this.y = canvas.height;
    } else if (this.y > canvas.height) {
        this.y = 0;
    }
}

Particle.prototype.draw = function () {
    ctx.strokeStyle = "#ffffff";
    ctx.fillStyle = "#ffffff";
    ctx.beginPath();
    ctx.arc(this.x, this.y, this.size, 0, 2 * Math.PI);
    ctx.fill();
    ctx.stroke();
}

Particle.prototype.connectMouse = function () {
    if (this.mouseDistance() <= connectDistance && mouse.present === true) {
        ctx.strokeStyle = "#ffffff";
        ctx.lineWidth = 0.25;
        ctx.beginPath();
        ctx.moveTo(this.x, this.y);
        ctx.lineTo(mouse.x, mouse.y);
        ctx.stroke();
    }
}

Particle.prototype.update = function () {
    this.move();
    this.teleport();
    this.draw();
    this.connectMouse();
}

function drawFrame() {
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;

    ctx.clearRect(0, 0, canvas.width, canvas.height);

    particles.forEach((particle, indx) => {
        particle.update();
    });

    requestAnimationFrame(drawFrame);
}

for (let i = 0; i < count; i++) {
    particles.push(new Particle());
}

drawFrame();
