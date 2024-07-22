import Link from "next/link";
import Puzzle from "./puzzle";

export default function SideNavbar() {
  const categories = [
    "hobby",
    "travel",
    "food",
    "goal",
    "shopping",
    "electric",
  ];

  return (
    <div>
      <h1>Sidebar</h1>
      <ol>
        {categories.map(subject => (
          <li key={subject}>
            <p>
              <Link href={`/lesson/${subject}`}>{subject}</Link>
            </p>
          </li>
        ))}
      </ol>
    </div>
  );
}
