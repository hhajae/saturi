"use client";

import { usePathname } from "next/navigation";
import { useEffect, useState } from "react";
import SideNavbar from "../../../components/(lesson)/sidebar";
import Puzzle from "../../../components/(lesson)/puzzle";
import PuzzleInfo from "../../../components/(lesson)/puzzleInfo"; // 새로운 컴포넌트 import

export default function CategoryPage() {
  const pathname = usePathname();
  const [categoryId, setCategoryId] = useState<string | null>(null);
  const [selectedPuzzle, setSelectedPuzzle] = useState<number | null>(null); // 선택된 퍼즐 조각을 위한 상태

  useEffect(() => {
    const pathSegments = pathname.split("/");
    const category_id = pathSegments[pathSegments.length - 1];
    if (category_id) {
      setCategoryId(category_id);
    }
  }, [pathname]);

  return (
    <div className="flex h-full">
      <div className="w-1/5 bg-gray-100 p-4">
        <SideNavbar />
      </div>
      <div className="flex-grow flex flex-col items-center justify-center p-4">
        {categoryId && <Puzzle id={categoryId} onSelect={setSelectedPuzzle} />}
      </div>
      <div className="w-1/5 bg-gray-100 p-4">
        {selectedPuzzle !== null && <PuzzleInfo id={selectedPuzzle} />}
      </div>
    </div>
  );
}
