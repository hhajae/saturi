import Link from "next/link";

interface SideNavbarProps {
  location: string;
}

export default function SideNavbar({ location }: SideNavbarProps) {
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
        {categories.map(category => (
          <li key={category.name}>
            <p className="flex items-center space-x-2 border border-gray-200 rounded-lg">
              <span>{category.icon}</span>
              <Link href={`/lesson/${location}/${category.name}`} className="text-lg">
                {category.name}
              </Link>
            </p>
          </li>
        ))}
      </ol>
    </div>
  );
}
