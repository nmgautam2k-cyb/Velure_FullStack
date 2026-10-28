import Link from "next/link";
import HeroSlideshow from "../components/HeroSlideshow";

export default function Home() {
  return (
    <div className="home-container">
  
      <HeroSlideshow />

      <section className="section">
        <h2 className="section-title">CATEGORIES</h2>
        <div className="category-grid">
          <Link href="/shirts" className="category-card">
            <img src="/shirt.avif" alt="Shirts" />
            <div className="category-card-label">
              <h4>SHIRTS</h4>
              <span className="category-card-arrow">SHOP NOW →</span>
            </div>
          </Link>

          <Link href="/pants" className="category-card">
            <img src="/pant.avif" alt="Pants" />
            <div className="category-card-label">
              <h4>PANTS</h4>
              <span className="category-card-arrow">SHOP NOW →</span>
            </div>
          </Link>

          <Link href="/tshirts" className="category-card">
            <img src="/Tshirt.avif" alt="T-Shirts" />
            <div className="category-card-label">
              <h4>T-SHIRTS</h4>
              <span className="category-card-arrow">SHOP NOW →</span>
            </div>
          </Link>
        </div>
      </section>

      <section className="section bg-light">
        <h2 className="section-title">NEW ARRIVALS</h2>
        <div className="product-grid arrivals-grid">
          <Link href="/shirts" className="card">
            <img src="/newarrival1.webp" alt="New Arrival 1" />
            <h4 style={{marginTop: "10px"}}>Top 1</h4>
          </Link>
          <Link href="/pants" className="card">
            <img src="/newarrival2.webp" alt="New Arrival 2" />
            <h4 style={{marginTop: "10px"}}>Top 2</h4>
          </Link>
          <Link href="/tshirts" className="card">
            <img src="/newarrival3.webp" alt="New Arrival 3" />
            <h4 style={{marginTop: "10px"}}>Top 3</h4>
          </Link>
        </div>
      </section>

      <section className="section discount-section">
        <h2 className="section-title">EXCLUSIVE DEALS</h2>
        <p className="section-subtitle">Limited time offers — don't miss out</p>
        <div className="discount-grid">

          <div className="discount-card discount-card--light">
            <span className="discount-badge">SHIRTS</span>
            <div className="discount-icon">👔</div>
            <div className="discount-percent">30%</div>
            <p className="discount-label">OFF</p>
            <p className="discount-desc">Flat discount on our entire shirts collection</p>
            <Link href="/shirts" className="discount-btn">Shop Shirts →</Link>
          </div>

          <div className="discount-card discount-card--dark discount-card--featured">
            <span className="discount-badge discount-badge--gold">HOT DEAL</span>
            <div className="discount-icon">👕</div>
            <div className="discount-percent">1+1</div>
            <p className="discount-label">FREE</p>
            <p className="discount-desc">Buy one T-Shirt, get one absolutely free</p>
            <Link href="/tshirts" className="discount-btn discount-btn--light">Shop T-Shirts →</Link>
          </div>

          <div className="discount-card discount-card--light">
            <span className="discount-badge">PANTS</span>
            <div className="discount-icon">👖</div>
            <div className="discount-percent">50%</div>
            <p className="discount-label">OFF</p>
            <p className="discount-desc">Up to half price on our premium pants range</p>
            <Link href="/pants" className="discount-btn">Shop Pants →</Link>
          </div>

        </div>
      </section>

      {/* FOOTER */}
      <footer className="footer">
        <div>
          <h2>VELURE</h2>
          <p>Email: support@velure.com | Phone: +1 234 567 890</p>
        </div>
      </footer>
    </div>
  );
}