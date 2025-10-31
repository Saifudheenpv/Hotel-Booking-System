// Hotel Booking System - Enhanced Features
class HotelBookingApp {
    constructor() {
        this.init();
    }

    init() {
        this.initEventListeners();
        this.initSearchFilters();
        this.initThemeToggle();
        this.showWelcomeMessage();
    }

    initEventListeners() {
        // Search functionality
        const searchInput = document.getElementById('searchInput');
        if (searchInput) {
            searchInput.addEventListener('input', this.debounce(this.handleSearch.bind(this), 300));
        }

        // Hotel card interactions
        document.querySelectorAll('.hotel-card').forEach(card => {
            card.addEventListener('click', (e) => {
                if (!e.target.closest('.btn')) {
                    const hotelId = card.dataset.hotelId;
                    this.viewHotelDetails(hotelId);
                }
            });
        });
    }

    initSearchFilters() {
        this.currentFilters = {
            rating: null,
            location: '',
            priceRange: null
        };
    }

    initThemeToggle() {
        const themeToggle = document.getElementById('themeToggle');
        if (themeToggle) {
            themeToggle.addEventListener('click', () => {
                document.body.classList.toggle('dark-theme');
                const isDark = document.body.classList.contains('dark-theme');
                localStorage.setItem('theme', isDark ? 'dark' : 'light');
                this.updateThemeIcon(isDark);
                this.showToast('Theme changed!', 'info');
            });

            // Load saved theme
            const savedTheme = localStorage.getItem('theme');
            if (savedTheme === 'dark') {
                document.body.classList.add('dark-theme');
                this.updateThemeIcon(true);
            }
        }
    }

    updateThemeIcon(isDark) {
        const themeToggle = document.getElementById('themeToggle');
        if (themeToggle) {
            const icon = themeToggle.querySelector('i');
            if (icon) {
                icon.className = isDark ? 'fas fa-sun' : 'fas fa-moon';
            }
        }
    }

    handleSearch(event) {
        const searchTerm = event.target.value.toLowerCase();
        const hotelCards = document.querySelectorAll('.hotel-card');
        
        hotelCards.forEach(card => {
            const hotelName = card.querySelector('.hotel-name').textContent.toLowerCase();
            const hotelLocation = card.querySelector('.hotel-location').textContent.toLowerCase();
            const hotelDescription = card.querySelector('.hotel-description').textContent.toLowerCase();
            
            const matchesSearch = hotelName.includes(searchTerm) || 
                                hotelLocation.includes(searchTerm) || 
                                hotelDescription.includes(searchTerm);
            
            card.style.display = matchesSearch ? 'block' : 'none';
        });
        
        this.updateResultsCount();
    }

    updateResultsCount() {
        const visibleCards = document.querySelectorAll('.hotel-card[style=""]').length + 
                           document.querySelectorAll('.hotel-card:not([style])').length;
        const resultsCount = document.querySelector('.results-count');
        if (resultsCount) {
            resultsCount.textContent = `${visibleCards} hotels found`;
        }
    }

    viewHotelDetails(hotelId) {
        // This will be handled by Spring Boot controller
        console.log('Viewing hotel details for:', hotelId);
    }

    showWelcomeMessage() {
        if (window.location.pathname === '/') {
            setTimeout(() => {
                this.showToast('Welcome to LuxStay! Discover luxury hotels worldwide.', 'info');
            }, 1000);
        }
    }

    showToast(message, type = 'info') {
        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        toast.textContent = message;
        
        document.body.appendChild(toast);
        
        setTimeout(() => toast.classList.add('show'), 100);
        setTimeout(() => {
            toast.classList.remove('show');
            setTimeout(() => toast.remove(), 300);
        }, 3000);
    }

    debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    }
}

// Initialize app when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.hotelApp = new HotelBookingApp();
    
    // Auto-dismiss flash messages after 5 seconds
    setTimeout(() => {
        const alerts = document.querySelectorAll('.alert');
        alerts.forEach(alert => {
            const bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        });
    }, 5000);
});