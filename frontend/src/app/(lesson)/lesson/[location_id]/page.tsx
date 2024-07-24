import { redirect } from "next/navigation";

export default function RedirectPage(id: number) {
  const locations = ["경상도", "강원도", "경기도", "전라도", "충청도"];
  redirect(`/lesson/${locations[id]}/hobby`);
  // return (
  //   <div>
  //     <h1>/lesson Page</h1>
  //     <h2>it will redirect to lesson/hobby</h2>
  //   </div>
  // );
}
