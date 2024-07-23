"use client"; // 클라이언트 컴포넌트로 지정

import { useState, useRef } from 'react';
import Image from 'next/image';
import './style.css';

// 헤더 파트
function Header({ onProfileClick }) {
  return (
    <div>
      <nav className="navbar">
        <ul className="nav-list">
          <li className="nav-item logo">
            <a href="">
             <img src="/MainPage/logo.png" alt="LOGO" width={250} />
            </a>
          </li>          
          <li className="nav-item profile"><a href="#" onClick={onProfileClick}>Profile</a></li>
        </ul>
      </nav>
      <br/>
    </div>
  );
}

// 프로필 모달 파트
function Modal({ isOpen, onClose }) {
  if (!isOpen) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <button className="close-button" onClick={onClose}>X</button>
        <h2>Profile Modal</h2>
        <p>This is the profile modal content.</p>
      </div>
    </div>
  );
}





// Left, Middle, Right 파트
function LeftPart({middlePosition}) {
  return (
    <div className="leftpart" style={{ 
      zIndex: (() => {
        if (middlePosition === 0) {
          return 0;
        } else if (middlePosition === 1) {
          return 2;
        } else if (middlePosition === 2) {
          return 2;
        } else {
          return null;
        }
      })()
    }}>
      LeftPart
    </div>
  );
}

function MiddlePart({ middlePosition }) {
  return (
    <div className="middlepart" style={{ 
      left: (() => {
        if (middlePosition === 0) {
          return "-110%";
        } else if (middlePosition === 1) {
          return "0%";
        } else if (middlePosition === 2) {
          return "110%";
        } else {
          return null;
        }
      })()
    }}>
      MiddlePart
      
    </div>
  );
}

function RightPart() {
  return (
    <div className="rightpart">
      RightPart
    </div>
  );
}


////////////////////////
/// 버튼과 맵 
////////////////////////

function ButtonPart({ onLeftClick, onRightClick }) {
  return (
    <div>
      <a href="#" className='buttonLeft' onClick={onLeftClick}>
        <img src="/MainPage/buttonLeft.png" alt="button" width={50} />
      </a>
      <a href="#" className='buttonRight' onClick={onRightClick}>
        <img src="/MainPage/buttonRight.png" alt="button" width={50} />
      </a>
    </div>
  );
}

function MiddleMap({ left }) {
  return (
    <div className="middleMap" style={{ left: left === "null" ? null : left }}>
      <Image
        src="/MainPage/map_of_korea.jpg"
        alt="koreamap"
        width={500}
        height={500}
      />
    </div>
  );
}





export default function App() {
  const [isModalOpen, setModalOpen] = useState(false);
  const [mapLeft, setMapLeft] = useState('50%'); // 초기 left 값을 50%로 설정
  const [middleToWhere, setMiddleToWhere] = useState(1); // 0, 1, 2 이 세 가지 값을 사용
  const currentMainPageRef = useRef(1);

  const handleProfileClick = (e) => {
    e.preventDefault();
    setModalOpen(true);
  };

  const closeModal = () => {
    setModalOpen(false);
  };

  const handleLeftClick = (e) => {
    e.preventDefault();
    if (currentMainPageRef.current === 1) {
      setMapLeft("80%");
      setMiddleToWhere(2);
      currentMainPageRef.current = 0; // 값 변경
    } else if (currentMainPageRef.current === 2) {
      setMapLeft("50%");
      setMiddleToWhere(1);
      currentMainPageRef.current = 1; // 값 변경
    }
  };

  const handleRightClick = (e) => {
    e.preventDefault();
    if (currentMainPageRef.current === 1) {
      setMapLeft("20%");
      setMiddleToWhere(0);
      currentMainPageRef.current = 2; // 값 변경
    } else if (currentMainPageRef.current === 0) {
      setMapLeft("50%");
      setMiddleToWhere(1);
      currentMainPageRef.current = 1; // 값 변경
    }
  };

  return (
    <div>
      <Header onProfileClick={handleProfileClick} />
      <hr />
      <div style={{ position: 'relative', width: '100%', height: '100vh' }}>
        <LeftPart middlePosition = {middleToWhere} />
        <MiddlePart middlePosition = {middleToWhere} />
        <RightPart />
        <ButtonPart onLeftClick={handleLeftClick} onRightClick={handleRightClick} />
        <MiddleMap left={mapLeft}/>
      </div>
      <Modal isOpen={isModalOpen} onClose={closeModal} />
    </div>
  );
}
