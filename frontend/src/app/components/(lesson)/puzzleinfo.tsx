interface PuzzleInfoProps {
  id: number;
}

export default function PuzzleInfo({ id }: PuzzleInfoProps) {
  return (
    <div className="bg-white p-12 rounded shadow">
      <h1 className="text-xl font-bold whitespace-nowrap">퍼즐 조각 정보</h1>
      <p className="whitespace-nowrap">{id}번 퍼즐 조각에 대한 정보</p>
      {/* 추가적인 퍼즐 조각 정보를 여기에 추가 */}
    </div>
  );
}
