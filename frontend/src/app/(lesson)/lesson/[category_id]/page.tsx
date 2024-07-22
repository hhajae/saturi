"use client";

import { usePathname } from "next/navigation";
import { useEffect, useState } from "react";
import SideNavbar from "../../../components/(lesson)/sidebar";
import Puzzle from "../../../components/(lesson)/puzzle";

export default function CategoryPage() {
  const pathname = usePathname();
  const [categoryId, setCategoryId] = useState<string | null>(null);

  useEffect(() => {
    const pathSegments = pathname.split("/");
    // eslint-disable-next-line @typescript-eslint/naming-convention
    const category_id = pathSegments[pathSegments.length - 1];
    if (category_id) {
      setCategoryId(category_id);
    }
  }, [pathname]);

  return (
    <div>
      <h1>Lesson: {categoryId} page</h1>
      <h2 className="text-blue-500">Sidebar, Puzzle, Info가 생길 것</h2>
      <br />
      <div className="flex">
        <div>
          <SideNavbar />
        </div>
        <div>{categoryId && <Puzzle category={categoryId} />}</div>
      </div>
    </div>
  );
}
