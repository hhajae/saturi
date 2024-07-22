import { useRouter } from "next/navigation";
import { Card, CardBody } from "@nextui-org/react";
import Image from "next/image";

interface PuzzleProps {
  id: string;
  onSelect: (pieceId: number) => void; // 클릭된 퍼즐 조각을 부모에게 전달하는 함수
}

// async function getPuzzle(id: string) {
//   const response = await fetch(`문제그룹 받아오기/${id}`);
//   return response.json();
// }

export default function Puzzle({ id, onSelect }: PuzzleProps) {
  // const pieces = await getPuzzle(id);
  const pieces = [1, 2, 3, 4, 5, 6, 7, 8, 9];
  const router = useRouter();
  const onClick = (piece: number) => {
    onSelect(piece); // 선택된 퍼즐 조각을 부모에게 전달
  };

  return (
    <div grid->
      <div className="grid grid-cols-3 gap-4 w-full">
        {pieces.map(piece => (
          <Card
            key={piece}
            className="flex border border-gray-500 p-4 cursor-pointer"
            onClick={() => onClick(piece)}
          >
            <CardBody>
              <p className="text-center m-24 text-2xl">{piece}</p>
            </CardBody>
          </Card>
        ))}
      </div>
    </div>
  );
}
