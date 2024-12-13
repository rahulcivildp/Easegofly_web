const container = document.getElementById("container");
const registerBtn = document.getElementById("register");
const signup = document.querySelector('.sign-up');
const signin = document.querySelector('.sign-in');

const loginBtn = document.getElementById("login");

registerBtn.addEventListener("click", () => {
    container.classList.add("active");
    signin.classList.add("hide-login-menu");
    signup.classList.remove("hide-login-menu");
});

loginBtn.addEventListener("click", () => {
    container.classList.remove("active");
    signup.classList.add("hide-login-menu");
    signin.classList.remove("hide-login-menu");
});

if(window.location.href.includes("signup")) {
    container.classList.add("active");
    signin.classList.add("hide-login-menu");
    signup.classList.remove("hide-login-menu");
	
}