import { redirect } from "next/navigation";

export default function App() {
  if ("login") {
    redirect("/main");
  } else {
    redirect("/start");
  }
}