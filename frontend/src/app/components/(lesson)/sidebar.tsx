import Link from "next/link";

export default function SideNavbar() {
  const categories = [
    { name: "hobby", icon: "🎨" },
    { name: "travel", icon: "✈️" },
    { name: "food", icon: "🍽️" },
    { name: "goal", icon: "🎯" },
    { name: "shopping", icon: "🛒" },
    { name: "electric", icon: "💻" },
  ];

  return (
    <div>
      <h1 className="text-2xl font-bold mb-4">Sidebar</h1>
      <ol className="space-y-4">
        {categories.map(subject => (
          <li key={subject.name}>
            <p className="flex items-center space-x-2 border border-gray-200 rounded-lg">
              <span>{subject.icon}</span>
              <Link href={`/lesson/${subject.name}`} className="text-lg">
                {subject.name}
              </Link>
            </p>
          </li>
        ))}
      </ol>
    </div>
  );
}
