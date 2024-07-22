import { CheckboxGroup, Checkbox } from "@nextui-org/react";
import { useState } from "react";

export default function SideNavbar() {
  const categories = [
    { name: "hobby", icon: "🎨" },
    { name: "travel", icon: "✈️" },
    { name: "food", icon: "🍽️" },
    { name: "goal", icon: "🎯" },
    { name: "shopping", icon: "🛒" },
    { name: "electric", icon: "💻" },
  ];
  const [selected, setSelected] = useState([]);

  return (
    <div>
      <CheckboxGroup
        label="원하는 주제를 고르세요"
        className="border border-black justify-center"
        value={selected}
        onValueChange={setSelected}
      >
        <Checkbox value={categories[0].name}>
          {categories[0].name}
          {categories[0].icon}
        </Checkbox>
        <Checkbox value={categories[1].name}>
          {categories[1].name}
          {categories[1].icon}
        </Checkbox>
        <Checkbox value={categories[2].name}>
          {categories[2].name}
          {categories[2].icon}
        </Checkbox>
        <Checkbox value={categories[3].name}>
          {categories[3].name}
          {categories[3].icon}
        </Checkbox>
        <Checkbox value={categories[4].name}>
          {categories[4].name}
          {categories[4].icon}
        </Checkbox>
        <Checkbox value={categories[5].name}>
          {categories[5].name}
          {categories[5].icon}
        </Checkbox>
      </CheckboxGroup>
      <p>현재 카테고리 :{selected.join(",")}</p>
    </div>
  );
}
