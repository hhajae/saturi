"use client";

import { usePathname } from "next/navigation";
import { useEffect, useState } from "react";
import SideNavbar from "../../../../../components/lesson/sidebar";
import Puzzle from "../../../../../components/lesson/puzzle";
import PuzzleInfo from "../../../../../components/lesson/puzzleInfo";

export default function CategorySelectPage() {
  const pathname = usePathname();
  const [categoryId, setCategoryId] = useState<string | null>(null);
  const [selectedPuzzle, setSelectedPuzzle] = useState<number | null>(null);

  useEffect(() => {
    const pathSegments = pathname.split("/");
    const nowCategory = pathSegments[pathSegments.length - 2];
    if (nowCategory) {
      setCategoryId(nowCategory);
    }
  }, [pathname]);

  return (
    <div className="flex flex-col h-screen justify-center items-center">
      <div className="flex w-full max-w-screen-lg h-5/6 items-center">
        <div className="w-1/5 bg-gray-100 p-4">
          <SideNavbar location={categoryId}/>
        </div>
        <div className="flex-grow flex flex-col items-center justify-center p-4 ">
          {categoryId && (
            <Puzzle id={categoryId} onSelect={setSelectedPuzzle} />
          )}
        </div>
        <div className="w-1/5 p-4 flex items-center">
          {selectedPuzzle !== null && <PuzzleInfo id={selectedPuzzle} />}
        </div>
      </div>
    </div>
  );
}
