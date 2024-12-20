import api from "@/lib/axios";
import { AxiosResponse } from "axios";
import { deleteCookie, getCookies, setCookie } from "cookies-next";
import { HandleLoginProps } from "@/utils/props";

// 쿠키 삽입
export function insertCookie(response: AxiosResponse) {
  const { data } = response;

  if (typeof data === "string" || data === null) {
    throw new Error("Could not insert cookie");
  }
  Object.keys(data).forEach(key => {
    const value = data[key];
    if (typeof value === "string") {
      setCookie(key, value);
    } else {
      setCookie(key, JSON.stringify(value));
    }
  });
}

// 유저 정보 받기
export function getUserInfo() {
  api.get("/user/auth/profile")
    .then(response => insertCookie(response))
}

// 로그인
export function handleLogin({
  email,
  password,
  router,
  goTo,
}: HandleLoginProps) {
  api
    .post("/user/auth/login", {
      email,
      password,
      userType: "NORMAL",
    })
    .then(response => {
      sessionStorage.setItem("accessToken", response.data.accessToken);
      sessionStorage.setItem("refreshToken", response.data.refreshToken);

      let destination = goTo
      if (response.data.role === "ADMIN") {
        destination = "/admin"
      }
      return destination;
    })
    .then((destination) => {
      api.get("user/auth/profile")
      .then(response => {
        insertCookie(response);
        router.push(`${destination}`);
        // window.location.href =`${process.env.NEXT_PUBLIC_FRONTURL}${destination}`
      })
    })
    .catch(error => {
      if (error.response.status === 400) {
        alert(error.response.data.msg);
      }
    });
}

// 소셜 로그인
export function goSocialLogin(provider: string) {
  const redirectUrl = `${process.env.NEXT_PUBLIC_FRONTURL}/user/auth/login/${provider}`;

  switch (provider) {
    case "kakao":
      window.location.href = `https://kauth.kakao.com/oauth/authorize?client_id=${process.env.NEXT_PUBLIC_KAKAOSECRET}&redirect_uri=${redirectUrl}&response_type=code`;
      break;
    case "naver":
      window.location.href = `https://nid.naver.com/oauth2.0/authorize?response_type=code&client_id=${process.env.NEXT_PUBLIC_NAVERKEY}&client_secret=${process.env.NEXT_PUBLIC_NAVERSECRET}&redirect_uri=${redirectUrl}&state=8697240`;
      break;
  }
}

export async function frontLogOut() {
  // 세션 스토리지에서 토큰 제거
  sessionStorage.removeItem("accessToken");
  sessionStorage.removeItem("refreshToken");
  sessionStorage.removeItem("adminToken");
  const cookies = getCookies();

  // 쿠키를 가져와서 삭제
  async function deleteCookies() {
    const cookieNames = Object.keys(cookies);
    await cookieNames.reduce(async (promise, cookieName) => {
      await promise;
      deleteCookie(cookieName);
    }, Promise.resolve());
  }
  await deleteCookies();
  // 쿠키 삭제 후 리다이렉션
}

// 토큰 유효성 확인
// 페이지 옮길때마다 실행 (메인의 authutils에 달려있음)
export function authToken() {
  api.get("/user/auth/token-check")
    .then(response => {
      if (response.status === 200) getUserInfo()
      }
    )
    .catch(err => {
      // 401 에러 발생 시
      if (err.response.status === 401) {
        // 리프레시 토큰을 들고 토큰 리프레시 신청하러감
        api.post(
            "/user/auth/token-refresh",
            {},
            {
              headers: {
                refreshToken: `${sessionStorage.getItem("refreshToken")}`,
              },
            },
          )
          .then(response => {
            sessionStorage.setItem("accessToken", response.data.accessToken);
          })
          // 만약 여기서도 401 뜨면, 로그아웃 처리 하고 로그인으로 보내기
          // 문제 1) 어차피 안되는거 500도 초기로 돌려야되는거아님?
          .catch(() => {
            frontLogOut().then(() => {
              alert("장시간 이용이 없어 초기화면으로 돌아갑니다.");
              window.location.href =`${process.env.NEXT_PUBLIC_FRONTURL}`
            });
          });
      }
    });
}