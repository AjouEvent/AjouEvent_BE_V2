import React from "react";
import styled from "styled-components";
import { Link } from "react-router-dom";

const StyledLink = styled(Link)`
  display: flex;
  align-items: center;
  justify-content: center;
  width: 5rem;
  height: 5rem;
  margin: 0 1rem 0 1rem;
  object-fit: contain;
`;

function NavButton(props) {
  return (
    <>
      {props.selected ? (
        <StyledLink to={`${props.link}`}>
          <img
            src={`${process.env.PUBLIC_URL}/icons/Nav${props.icon}On.svg`}
            alt={`${props.icon}`}
          />
        </StyledLink>
      ) : (
        <StyledLink to={`${props.link}`}>
          <img
            src={`${process.env.PUBLIC_URL}/icons/Nav${props.icon}Off.svg`}
            alt={`${props.icon}`}
          />
        </StyledLink>
      )}
    </>
  );
}

export default NavButton;
