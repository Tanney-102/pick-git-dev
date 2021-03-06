import { BrowserRouter, Route, Switch } from "react-router-dom";

import { PAGE_URL } from "./constants/urls";
import LoginPage from "./pages/LoginPage/LoginPage";
import NavigationHeader from "./components/@layout/NavigationHeader/NavigationHeader";
import HomeFeedPage from "./pages/HomeFeedPage/HomeFeedPage";

const App = () => {
  return (
    <BrowserRouter>
      <Switch>
        <Route path={[PAGE_URL.HOME, PAGE_URL.MY_PROFILE]}>
          <NavigationHeader isLoggedIn={false} />
        </Route>
      </Switch>
      <Switch>
        <Route path={PAGE_URL.LOGIN}>
          <LoginPage />
        </Route>
        <Route path={PAGE_URL.HOME}>
          <HomeFeedPage />
        </Route>
      </Switch>
    </BrowserRouter>
  );
};

export default App;
