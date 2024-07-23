import { Card, CardBody } from "@nextui-org/react";

interface IPuzzlePiece {
  id: number;
}

export default function PuzzlePiece({ id }: IPuzzlePiece) {
  return (
    <div>
      <Card className="w-24 h-24">
        <CardBody className="items-center justify-center">
          <p>{id}</p>
        </CardBody>
      </Card>
    </div>
  );
}
