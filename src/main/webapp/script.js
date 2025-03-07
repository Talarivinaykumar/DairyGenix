/**
 * 
 */
// Function to handle button navigation
function navigateTo(page) {
    window.location.href = page; // Redirects to the specified page
}

// Event listeners for the buttons
document.getElementById("homeBtn").addEventListener("click", function() {
    navigateTo("index.html"); // Navigates to the Home page
});

document.getElementById("productsBtn").addEventListener("click", function() {
    navigateTo("products.html"); // Navigates to the Products page
});

document.getElementById("aboutUsBtn").addEventListener("click", function() {
    navigateTo("about.html"); // Navigates to the About Us page
});

document.getElementById("contactBtn").addEventListener("click", function() {
    navigateTo("contact.html"); // Navigates to the Contact page
});


let currentIndex = 0;
const slides = document.querySelectorAll('.slide');
const totalSlides = slides.length;

function showNextSlide() {
    slides[currentIndex].classList.remove('active');
    currentIndex = (currentIndex + 1) % totalSlides;
    slides[currentIndex].classList.add('active');
    const offset = -currentIndex * 100;
    document.querySelector('.slider-container').style.transform = `translateX(${offset}%)`;
}

// Automatically change slide every 4 seconds
setInterval(showNextSlide, 1000);
