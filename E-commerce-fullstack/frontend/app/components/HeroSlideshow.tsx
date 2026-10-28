"use client";

import { useState, useEffect, useCallback } from "react";
import Link from "next/link";

const images = [
  "/velure_men_hero_1.png",
  "/velure_men_hero_2.png",
  "/velure_men_hero_3.png",
];

export default function HeroSlideshow() {
  const [currentIndex, setCurrentIndex] = useState(0);
  const [isTransitioning, setIsTransitioning] = useState(true);

  // Create extended images array with duplicated first image at the end for seamless loop
  const extendedImages = [...images, images[0]];

  const goToNext = useCallback(() => {
    if (currentIndex >= images.length) {
      // At the duplicated slide, jump back to first without animation
      setIsTransitioning(false);
      setCurrentIndex(0);
    } else {
      setIsTransitioning(true);
      setCurrentIndex((prev) => prev + 1);
    }
  }, [currentIndex, images.length]);

  useEffect(() => {
    const timer = setInterval(() => {
      goToNext();
    }, 1500);
    return () => clearInterval(timer);
  }, [goToNext]);

  // Handle the instant jump after transition is disabled
  useEffect(() => {
    if (!isTransitioning) {
      const timeout = setTimeout(() => {
        setIsTransitioning(true);
      }, 50);
      return () => clearTimeout(timeout);
    }
  }, [isTransitioning]);

  const handleDotClick = (index: number) => {
    setIsTransitioning(true);
    setCurrentIndex(index);
  };

  // Calculate display index for dots (handle the duplicated slide case)
  const displayIndex = currentIndex >= images.length ? 0 : currentIndex;

  return (
    <section style={{ width: "100%", margin: 0, padding: 0, position: "relative", height: "80vh", overflow: "hidden" }}>

      {/* Slider Track - moves continuously from right to left (images appear from left to right) */}
      <div style={{
        display: "flex",
        width: `${extendedImages.length * 100}%`,
        height: "100%",
        transform: `translateX(-${currentIndex * (100 / extendedImages.length)}%)`,
        transition: isTransitioning ? "transform 1s cubic-bezier(0.25, 0.8, 0.25, 1)" : "none"
      }}>
        {extendedImages.map((src, index) => (
          <div key={index} style={{ width: `${100 / extendedImages.length}%`, height: "100%" }}>
            <img
              src={src}
              alt={`Velure Collection ${(index % images.length) + 1}`}
              style={{
                width: "100%",
                height: "100%",
                objectFit: "cover",
                objectPosition: "center top"
              }}
            />
          </div>
        ))}
      </div>

      {/* Dark gradient overlay for text readability */}
      <div style={{
        position: "absolute",
        top: 0,
        left: 0,
        width: "100%",
        height: "100%",
        background: "linear-gradient(to top, rgba(0,0,0,0.7) 0%, rgba(0,0,0,0.2) 50%, rgba(0,0,0,0) 100%)"
      }} />

      {/* Content overlay */}
      <div style={{
        position: "absolute",
        bottom: "15%",
        left: "50%",
        transform: "translateX(-50%)",
        textAlign: "center",
        zIndex: 10,
        width: "100%",
        padding: "0 20px"
      }}>
        <h1 style={{
          color: "white",
          fontSize: "clamp(2.5rem, 8vw, 4rem)",
          marginBottom: "15px",
          letterSpacing: "2px",
          fontWeight: "700",
          textShadow: "0 4px 15px rgba(0,0,0,0.5)"
        }}>
          VELURE
        </h1>
        <p style={{
          color: "rgba(255,255,255,0.9)",
          fontSize: "clamp(1rem, 3vw, 1.3rem)",
          marginBottom: "35px",
          fontWeight: "400",
          letterSpacing: "1px"
        }}>
          MADE BY CONFIDENCE
        </p>
        <Link
          href="/shirts"
          className="shop-btn"
          style={{
            padding: "15px 40px",
            fontSize: "15px",
            borderRadius: "8px",
            display: "inline-block"
          }}
        >
          SHOP COLLECTIONS
        </Link>
      </div>

      {/* Dots Indicator */}
      <div style={{
        position: "absolute",
        bottom: "5%",
        left: "50%",
        transform: "translateX(-50%)",
        display: "flex",
        gap: "12px",
        zIndex: 10
      }}>
        {images.map((_, index) => (
          <div
            key={index}
            onClick={() => handleDotClick(index)}
            style={{
              width: "10px",
              height: "10px",
              borderRadius: "50%",
              background: index === displayIndex ? "white" : "rgba(255, 255, 255, 0.4)",
              cursor: "pointer",
              transition: "all 0.3s ease",
              boxShadow: "0 2px 5px rgba(0,0,0,0.3)",
              transform: index === displayIndex ? "scale(1.2)" : "scale(1)"
            }}
          />
        ))}
      </div>
    </section>
  );
}
